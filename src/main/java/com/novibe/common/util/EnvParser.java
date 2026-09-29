package com.novibe.common.util;

import com.novibe.common.base_structures.DnsProfile;
import com.novibe.common.config.EnvironmentVariables;
import com.novibe.common.exception.UserInputException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

public class EnvParser {

    public static List<String> parse(String envValue) {
        ArrayList<String> parsed = new ArrayList<>();
        if (isNull(envValue)) return parsed;
        for (String value : envValue.split(",")) {
            String strippedValue = value.strip();
            if (!strippedValue.isEmpty()) {
                parsed.add(strippedValue);
            }
        }
        return parsed;
    }

    public static List<DnsProfile> parseProfiles() {
        List<String> dnsList = parse(EnvironmentVariables.DNS);
        List<String> clientIdList = parse(EnvironmentVariables.CLIENT_ID);
        List<String> secretList = parse(EnvironmentVariables.AUTH_SECRET);
        List<String> donorList = parse(EnvironmentVariables.DONOR_DNS);

        if (clientIdList.size() != secretList.size()) {
            throw UserInputException.noStackTrace("CLIENT_ID values amount and AUTH_SECRET values amount must be equal, but were %s and %s"
                    .formatted(clientIdList.size(), secretList.size()));
        }
        int profilesAmount = clientIdList.size();

        if (dnsList.size() == 1) {
            dnsList = Collections.nCopies(profilesAmount, dnsList.getFirst());
        } else if (dnsList.size() != profilesAmount) {
            throw UserInputException.noStackTrace("DNS values amount must be equal to CLIENT_ID values amount or contain exactly one provider");
        }

        donorList.replaceAll(val -> "-".equals(val) ? null : val);
        if (donorList.size() <= 1) {
            donorList = Collections.nCopies(profilesAmount, donorList.isEmpty() ? null : donorList.getFirst());
        } else if (donorList.size() != profilesAmount) {
            throw UserInputException.noStackTrace("DONOR_DNS values amount must be equal to CLIENT_ID values amount or contain exactly one provider");
        }
        ArrayList<DnsProfile> dnsProfiles = new ArrayList<>();

        for (int i = 0; i < profilesAmount; i++) {
            DnsProfile dnsProfile = DnsProfile.builder()
                    .dnsProvider(dnsList.get(i).toUpperCase())
                    .clientId(clientIdList.get(i))
                    .authSecret(secretList.get(i))
                    .donorDns(donorList.get(i))
                    .number(i + 1)
                    .build();
            dnsProfiles.add(dnsProfile);
        }
        return dnsProfiles;
    }

}
