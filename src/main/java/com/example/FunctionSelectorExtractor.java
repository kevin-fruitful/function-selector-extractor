package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class FunctionSelectorExtractor {
    public static void main(String[] args) throws IOException {
        String folderPath = "forge-artifacts/";
        File folder = new File(folderPath);
        File[] listOfFolders = folder.listFiles();

        Map<String, String> functionSignatureMap = new HashMap<>();

        // Filter folders based on conditions
        for (File subfolder : listOfFolders) {
            if (subfolder.isDirectory()) {
                String folderName = subfolder.getName();
                if (folderName.startsWith("I")
                        && (folderName.endsWith("Facet.sol") || "IDiamondCut.sol".equals(folderName)
                                && !folderName.contains("IUniswapV3"))) {
                    File jsonFile = new File(subfolder, folderName.replace(".sol", ".json"));
                    if (jsonFile.isFile()) {
                        String jsonContent = Files.readString(Path.of(jsonFile.getPath()));
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(jsonContent);
                        JsonNode methodIdentifiers = rootNode.get("methodIdentifiers");

                        if (methodIdentifiers != null) {
                            Iterator<Map.Entry<String, JsonNode>> fields = methodIdentifiers.fields();
                            while (fields.hasNext()) {
                                Map.Entry<String, JsonNode> field = fields.next();
                                String functionSignature = field.getKey();
                                String functionSelector = field.getValue().asText();
                                functionSignatureMap.put(functionSignature, functionSelector);
                            }
                        }
                    }
                }
            }
        }

        // Print function signatures and selectors
        functionSignatureMap.forEach((signature, selector) -> System.out
                .println("Function signature: " + signature + ", Function selector: " + selector));
    }
}
