package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.stream.Collectors.toMap;

public class FunctionSelectorExtractor {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static boolean isWanted(String folderName) {
        return folderName.startsWith("I")
                && (folderName.endsWith("Facet.sol") || "IDiamondCut.sol".equals(folderName) && !folderName.contains("IUniswapV3"));
    }

    public static void main(String[] args) {
        File folder = new File("forge-artifacts/");

        Map<String, String> functionSignatureMap = Arrays.stream(requireNonNull(folder.listFiles()))
                .filter(File::isDirectory)
                .filter(f -> FunctionSelectorExtractor.isWanted(f.getName()))
                .map(f -> new File(f, f.getName().replace(".sol", ".json")))
                .filter(File::isFile)
                .map(FunctionSelectorExtractor::pathToString)
                .map(FunctionSelectorExtractor::getMethodIdentifiers)
                .filter(Objects::nonNull)
                .flatMap(n -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(n.fields(), ORDERED), false))
                .collect(toMap(Map.Entry::getKey, (Map.Entry<String, JsonNode> e) -> e.getValue().asText()));

        // Print function signatures and selectors
        functionSignatureMap.forEach((signature, selector) -> System.out
                .println("Function signature: " + signature + ", Function selector: " + selector));
    }

    private static JsonNode getMethodIdentifiers(String f) {
        try {
            return mapper.readTree(f).get("methodIdentifiers");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String pathToString(File f) {
        try {
            return Files.readString(Path.of(f.getPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
