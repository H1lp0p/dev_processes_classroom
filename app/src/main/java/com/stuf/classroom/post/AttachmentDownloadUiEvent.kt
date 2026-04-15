package com.stuf.classroom.post

/**
 * Открытие прямой ссылки на файл ([GET] `api/files/{id}`).
 *
 * Внешний браузер/загрузчик не передаёт Bearer из приложения; если доступ только по JWT,
 * на бэкенде нужны cookie, подписанный URL или иной механизм.
 */
sealed interface AttachmentDownloadUiEvent {
    data class OpenUrl(val url: String) : AttachmentDownloadUiEvent

    data class Failure(val message: String) : AttachmentDownloadUiEvent
}
