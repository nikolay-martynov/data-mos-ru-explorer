package com.github.nikolaymartynov.datamosruexplorer

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class Settings {

    @NotBlank
    String apiKey

    @NotNull
    @Min(1L)
    Integer scanParallelism = 2

    @NotNull
    @Min(1L)
    @Max(50L)
    Integer maxFreshest = 30

    @NotNull
    @Min(1L)
    @Max(50L)
    Integer maxLargest = 10

}
