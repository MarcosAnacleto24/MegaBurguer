package com.example.megaburguer.domain.extract

import com.example.megaburguer.data.repository.extract.ExtractDataSourceImp
import jakarta.inject.Inject

class DeleteExtractUseCase @Inject constructor(
    private val extractDataSourceImp: ExtractDataSourceImp
) {
    suspend operator fun invoke() {
        extractDataSourceImp.deleteExtract()
    }
}