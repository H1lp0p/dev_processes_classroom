# Scripts

## `seed-self-assessment-course.ps1`

Создаёт на бэкенде курс и одиночное задание с включённой самооценкой (рубрика: `criteria` + `studentScoreWeight > 0`).

**Учётная запись:** `user@example.com` / `string1` (владелец курса и учитель).

**Тексты на русском** — в `seed-self-assessment-course.data.json` (UTF-8). Скрипт `.ps1` без кириллицы, чтобы PowerShell 5.x не ломал парсер из‑за кодировки.

```powershell
cd d:\_hits\grade_3\dev_processes\android\classroom
.\scripts\seed-self-assessment-course.ps1

# другой хост API:
.\scripts\seed-self-assessment-course.ps1 -BaseUrl "http://37.21.130.4:5000"
```

Использует HTTP-хелперы из `.temp/lib.ps1` (curl). В конце выводит `course id`, `invite code` и `post id` задания.
