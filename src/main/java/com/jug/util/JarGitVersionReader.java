package com.jug.util;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;

public class JarGitVersionReader {
    public boolean canReadJsonGitInformation() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream intputStream = classLoader.getResourceAsStream("git.properties");
        try {
            readFromInputStream(intputStream);
        } catch (IOException e) {
            return false;
        } catch (NullPointerException e){
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public String getJsonGitInformationString() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream intputStream = classLoader.getResourceAsStream("git.properties");
        try {
            return readFromInputStream(intputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
