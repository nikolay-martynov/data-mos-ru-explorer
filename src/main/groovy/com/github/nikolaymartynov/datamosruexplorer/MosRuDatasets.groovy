package com.github.nikolaymartynov.datamosruexplorer

import groovy.json.JsonSlurper
import org.springframework.stereotype.Service

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MosRuDatasets {

    String baseUrl = 'https://apidata.mos.ru/'

    Integer apiVersion = 1

    List<Dataset> findDatasets(String apiKey) {
        DateTimeFormatter dateParser = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        (getAndParse(apiKey, "v$apiVersion/datasets") as List<Map<String, ?>>).collect {
            new Dataset(
                    id: it['Id'] as long,
                    caption: it['Caption'] as String,
                    archive: it['IsArchive'] as boolean,
                    lastUpdateDate: LocalDate.parse(it['LastUpdateDate'] as String, dateParser))
        }
    }

    long getDatasetSize(String apiKey, long datasetId) {
        getAndParse(apiKey, "v$apiVersion/datasets/$datasetId/count") as long
    }

    private Object getAndParse(String apiKey, String path, Map<String, ?> query = null) {
        StringBuilder url = new StringBuilder(baseUrl)
        if (path) {
            url << path
        }
        url << "?api_key=$apiKey"
        if (query) {
            url << "&" << query.collect { "${it.key}=${it.value}" }.join('&')
        }
        new JsonSlurper().parse(url.toURL())
    }

}
