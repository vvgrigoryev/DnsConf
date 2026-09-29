package com.novibe.dns.cloudflare.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListServiceTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 999, 1000, 1001, 1500, 2000, 2001, 3000})
    void cutChunksKeepsAllItems(int size) {
        List<Integer> items = IntStream.range(0, size).boxed().toList();

        List<List<Integer>> chunks = ListService.cutChunks(items);

        assertEquals(items, chunks.stream().flatMap(List::stream).toList());
        assertEquals((size + 999) / 1000, chunks.size());
        assertTrue(chunks.stream().allMatch(chunk -> !chunk.isEmpty() && chunk.size() <= 1000));
    }
}
