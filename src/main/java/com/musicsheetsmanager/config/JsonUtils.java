package com.musicsheetsmanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> List<T> leggiDaJson(Path PATH_FILE_JSON, Type type) {
        try (Reader reader = new FileReader(PATH_FILE_JSON.toFile())) {
            List<T> lista = gson.fromJson(reader, type);
            return lista!= null ? lista : new ArrayList<>();    // lista vuota se file vuoto
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (IOException | JsonIOException e) {
            throw new RuntimeException("Errore lettura file JSON: " + PATH_FILE_JSON, e);
        }
    }

    public static <T> void scriviSuJson(List<T> lista, Path PATH_FILE_JSON){
        try(Writer writer = new FileWriter(PATH_FILE_JSON.toFile())) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            throw new RuntimeException("Errore salvataggio file JSON" + PATH_FILE_JSON, e);
        }
    }
}
