package com.novibe.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvParserTest {

    @Test
    void stripsWhitespaceAroundEachValue() {
        assertEquals(List.of("https://first.com/hosts", "https://second.com/hosts"),
                EnvParser.parse("https://first.com/hosts, https://second.com/hosts"));
        assertEquals(List.of("NEXTDNS", "CLOUDFLARE"), EnvParser.parse("NEXTDNS, CLOUDFLARE"));
    }

    @Test
    void skipsEmptyValues() {
        assertEquals(List.of("first", "second"), EnvParser.parse("first,,second,"));
    }

    @Test
    void returnsEmptyListForBlankValue() {
        assertEquals(List.of(), EnvParser.parse(""));
        assertEquals(List.of(), EnvParser.parse("   "));
        assertEquals(List.of(), EnvParser.parse(null));
    }
}
