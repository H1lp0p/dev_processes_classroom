# Classroom API seed (PowerShell + curl)

Скрипт в этой папке подготавливает тестовые данные, максимально близкие к офлайн-мокам проекта, через API из `app/data/classroom_api_v4.json`.

Base URL по умолчанию: `http://37.21.130.4:5000`.

## Что лежит в папке

- `seed.ps1` — основной сценарий сидирования.
- `lib.ps1` — обертки над `curl.exe` (retry, throttling, логи, парсинг ответов).
- `seed.data.json` — нормализованные тестовые данные (пользователи, курсы, посты, комментарии, решения, team-flow).
- `logs/` — создается автоматически во время запуска.

## Креды созданных пользователей

Общий пароль для всех:

- `ClassroomDemo2026!`

Пользователи:

- `student@demo.local` (Студент Демо) (new pass = `asdf123`)
- `teacher@demo.local` (Учитель Иванова)
- `alex@demo.local` (Алексей К.)
- `maria@demo.local` (Мария С.)
- `oleg@demo.local` (Олег В.)
- `anna@demo.local` (Анна Л.)

## Запуск (вручную)

Из папки `.temp`:

```powershell
powershell -ExecutionPolicy Bypass -File .\seed.ps1
```

Продолжить с конкретного этапа (без повторного создания предыдущих):

```powershell
powershell -ExecutionPolicy Bypass -File .\seed.ps1 -StartFrom posts
```

Доступные этапы `-StartFrom`:

- `auth`
- `courses`
- `posts`
- `comments`
- `replies`
- `solutions`
- `teamWorkflows`

С безопасной пробной прогонкой без реальных запросов:

```powershell
powershell -ExecutionPolicy Bypass -File .\seed.ps1 -DryRun
```

С более щадящей нагрузкой:

```powershell
powershell -ExecutionPolicy Bypass -File .\seed.ps1 -DelayMs 700 -MaxRetries 3
```

## Анти-дудос меры в скрипте

- Один поток, строго последовательные запросы.
- Пауза между запросами (`-DelayMs`, по умолчанию 500ms).
- Retry только на `429` и `5xx` с backoff.
- Мягкий режим для не-критичных операций (часть team-flow), чтобы не ломать весь прогон.

## Примечания

- Скрипт ничего не запускает автоматически.
- После каждого этапа сохраняется `.temp/seed.state.json` (id созданных курсов/постов и т.д.) для resume-запуска.
- Текущая версия `seed.data.json` исключает только комментарии к командным постам (из-за известной проблемы backend). Командные задания и team-flow оставлены.
- Формат auth-ответа в swagger общий (`ObjectApiResponse`), поэтому в `lib.ps1` сделан безопасный извлекатель токена и `id` по нескольким вариантам полей.
- Team/grade сценарии выполняются best-effort, т.к. поведение команд зависит от серверных правил (капитан, наличие автосозданных команд и т.п.).
