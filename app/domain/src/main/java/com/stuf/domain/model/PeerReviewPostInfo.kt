package com.stuf.domain.model

data class PeerReviewPostInfo(
    val isPeerToPeer: Boolean,
    val minReviewsRequired: Int?,
    val isTeamTask: Boolean,
)

fun TaskPost.peerReviewPostInfo(): PeerReviewPostInfo =
    PeerReviewPostInfo(
        isPeerToPeer = gradingMode == GradingMode.PEER_TO_PEER,
        minReviewsRequired = minPeerReviewsRequired,
        isTeamTask = false,
    )

fun TeamTaskPost.peerReviewPostInfo(): PeerReviewPostInfo =
    PeerReviewPostInfo(
        isPeerToPeer = gradingMode == GradingMode.PEER_TO_PEER,
        minReviewsRequired = null,
        isTeamTask = true,
    )

fun PeerReviewPostInfo.detailLines(progress: PeerReviewProgress?): List<String> {
    if (!isPeerToPeer) return emptyList()
    val lines = mutableListOf<String>()
    lines += "Режим оценивания: взаимная (P2P)"
    if (!isTeamTask) {
        minReviewsRequired?.let { lines += "Минимум оцениваний: $it" }
    } else {
        lines += "Нужно оценить хотя бы одну другую команду"
    }
    progress?.let { p ->
        lines += "Прогресс: ${p.completed} из ${p.required}"
        lines +=
            when {
                p.isCounted -> "Решение засчитано"
                p.canFinish -> "Можно завершить оценивание"
                else -> "Оценивание не завершено"
            }
    }
    return lines
}

fun PeerReviewPostInfo.navigationBlockMessage(hasSolution: Boolean): String? =
    when {
        !isPeerToPeer -> "P2P-оценивание недоступно для этого задания"
        !hasSolution -> "Сначала прикрепите своё решение"
        else -> null
    }

fun PeerReviewPostInfo.canOpenPeerReviewScreen(hasSolution: Boolean): Boolean =
    isPeerToPeer && hasSolution

/** Пояснение в блоке «P2P-оценивание» на экране задания. */
fun PeerReviewPostInfo.peerReviewSectionHint(): String =
    when {
        !isPeerToPeer -> ""
        isTeamTask ->
            "Команда сдаёт решение; каждый участник оценивает хотя бы одну другую команду " +
                "по рубрике. После этого задание засчитывается автоматически."
        else -> {
            val minText =
                minReviewsRequired?.let { "минимум $it чужих работ" }
                    ?: "чужие работы"
            "Сначала сдайте своё решение, затем оцените $minText по рубрике (автор скрыт). " +
                "Пока не выполните минимум и не нажмёте «Завершить оценивание», решение не будет засчитано."
        }
    }

/** Краткое пояснение в блоке «Оценка», если задание в режиме P2P. */
fun PeerReviewPostInfo.gradingSectionPeerReviewHint(): String? =
    if (!isPeerToPeer) {
        null
    } else {
        "Ваше решение засчитается только после оценивания чужих работ (см. блок «P2P-оценивание» выше)."
    }
