# Classroom Android
Android-приложение. Аналог Google Classroom.

# Простая установка
- [скачать apk](https://github.com/H1lp0p/dev_processes_classroom/releases/tag/1.0.0)
- Установить

## Требования
- Android Studio (рекомендуется актуальная stable-версия).
- Android SDK с `minSdk 26`.
- JDK 11 (проект собран на Java/Kotlin 11).

## Режимы запуска
В проекте есть два flavor-режима:
- `api` - приложение работает с реальным backend API.
- `offline` - приложение работает на локальных моках (демо-репозитории), без backend.

Оба режима доступны как Debug/Release варианты (например, `apiDebug`, `offlineDebug`).

## Запуск через Android Studio
1. Откройте проект в Android Studio и дождитесь sync Gradle.
2. В Build Variants выберите нужный вариант:
   - `apiDebug` для работы с backend.
   - `offlineDebug` для работы с локальными моками.
3. Запустите приложение на эмуляторе/устройстве.

## Запуск через Gradle (CLI)
Из корня проекта:

```bash
./gradlew :app:installApiDebug
```

```bash
./gradlew :app:installOfflineDebug
```

Для Windows PowerShell можно использовать:

```powershell
.\gradlew.bat :app:installApiDebug
.\gradlew.bat :app:installOfflineDebug
```

## Тестовые аккаунты
### Для режима `api`
| Email             | Password |
|-------------------|----------|
| user@example.com  | string1  |
| user1@example.com | string1  |

### Для режима `offline`
- Можно входить с любым email/password (логин и регистрация моковые).
- Данные экрана и курсов берутся из локального demo-store.

## Как поменять `baseUrl` для режима с backend
`baseUrl` для сетевого клиента задается в `BuildConfig.API_BASE_URL`.

1. Откройте файл `app/data/build.gradle.kts`.
2. Найдите строку в `defaultConfig`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://37.21.130.4:5000\"")
```

3. Замените URL на нужный, например:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://your-backend.example.com\"")
```

4. Пересинхронизируйте Gradle (`Sync Project with Gradle Files`) и запустите вариант `apiDebug`.

Важно:
- Используйте полный URL с протоколом (`http://` или `https://`).
- Значение применяется для `api` flavor (онлайн-режим), в `offline` режиме сеть не используется.
