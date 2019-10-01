package com.github.nikolaymartynov.datamosruexplorer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

import javax.validation.Valid
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.stream.Collectors

@Controller
class HomeController {

    @Autowired
    MosRuDatasets mosRuDatasets

    @GetMapping("/")
    String home(Model model) {
        model.addAttribute("settings", new Settings())
        "settings"
    }

    @PostMapping("/")
    String process(@Valid @ModelAttribute Settings settings, BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            return "settings"
        }
        Result result = new Result()
        model.addAttribute("result", result)
        List<Dataset> datasets = mosRuDatasets.findDatasets(settings.apiKey)
        result.totalDatasets = datasets.size()
        List<Dataset> currentDatasets = datasets.findAll { !it.archive }
        result.currentDatasets = currentDatasets.size()
        if (currentDatasets) {
            List<Dataset> newestDatasets = currentDatasets.toSorted {
                it.lastUpdateDate
            }.reverse()[0..[settings.maxFreshest - 1, currentDatasets.size()].min()]
            Map<Long, Dataset> datasetsById = datasets.collectEntries { [it.id, it] }
            ForkJoinPool pool = new ForkJoinPool(settings.scanParallelism)
            try {
                List<SizedDataset> sizedDatasets = pool.submit({
                    newestDatasets.parallelStream().map {
                        new SizedDataset(id: it.id, caption: it.caption, lastUpdateDate: it.lastUpdateDate,
                                size: mosRuDatasets.getDatasetSize(settings.apiKey, it.id))
                    }.collect(Collectors.toList())
                } as Callable<List>).get().sort { a, b -> b.size <=> a.size }
                result.datasets = sizedDatasets[0..[settings.maxLargest - 1, sizedDatasets.size()].min()]
            } finally {
                pool.shutdown()
            }
        }
        "result"
    }

}
