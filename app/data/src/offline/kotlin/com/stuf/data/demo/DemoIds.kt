package com.stuf.data.demo

import com.stuf.domain.model.CourseId
import com.stuf.domain.model.PostId
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.UserId
import java.util.UUID

internal object DemoIds {
    val userStudent: UserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    val userTeacher: UserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

    val courseAlgebra: CourseId = CourseId(UUID.fromString("11111111-1111-1111-1111-111111111101"))
    val courseWeb: CourseId = CourseId(UUID.fromString("22222222-2222-2222-2222-222222222202"))
    /** Курс, где демо-пользователь — преподаватель. */
    val courseTeacherClub: CourseId = CourseId(UUID.fromString("66666666-6666-6666-6666-666666666601"))

    val postWelcome: PostId = PostId(UUID.fromString("33333333-3333-3333-3333-333333333301"))
    val postHomework: PostId = PostId(UUID.fromString("33333333-3333-3333-3333-333333333302"))
    val postMaterialAlgebra: PostId = PostId(UUID.fromString("33333333-3333-3333-3333-333333333303"))
    val postTeamAlgebra: PostId = PostId(UUID.fromString("33333333-3333-3333-3333-333333333304"))
    /** Командное задание на курсе «Веб»: демо — вы капитан, оценка выставлена, дедлайн прошёл. */
    val postTeamWebSprint: PostId = PostId(UUID.fromString("44444444-4444-4444-4444-444444444404"))
    /** Капитан, решение ещё не отправлено — только черновик. */
    val postTeamWebCaptainDraft: PostId = PostId(UUID.fromString("44444444-4444-4444-4444-444444444405"))
    /** Дедлайн просрочен — отправка решения недоступна. */
    val postTeamOverdue: PostId = PostId(UUID.fromString("44444444-4444-4444-4444-444444444406"))
    val postWebLab: PostId = PostId(UUID.fromString("44444444-4444-4444-4444-444444444403"))
    val postClubWelcome: PostId = PostId(UUID.fromString("77777777-7777-7777-7777-777777777701"))

    val solutionHomework: SolutionId = SolutionId(UUID.fromString("55555555-5555-5555-5555-555555555501"))

    /** Решение командного задания «Веб-спринт» (приватные комментарии к нему в демо). */
    val solutionTeamWebSprint: SolutionId =
        SolutionId(UUID.fromString("99999999-9999-9999-9999-999999999901"))

    /** Решение просроченного командного задания (демо: участник, не капитан). */
    val solutionTeamOverdue: SolutionId =
        SolutionId(UUID.fromString("99999999-9999-9999-9999-999999999902"))

    /** Дополнительные «однокурсники» для составов команд в демо. */
    val userPeerAlex: UserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000011"))
    val userPeerMaria: UserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000012"))
    val userPeerOleg: UserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000013"))
    val userPeerAnna: UserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000014"))

    val teamAlgebraFull: TeamId = TeamId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa01"))
    val teamAlgebraPartial: TeamId = TeamId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02"))
    val teamAlgebraEmpty: TeamId = TeamId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa03"))
    val teamWebUiCrew: TeamId = TeamId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10"))
    val teamWebCaptainSolo: TeamId = TeamId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa11"))
    val teamWebOverdue: TeamId = TeamId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa12"))
}
