# Сюрреалистический Цирк Снов (Surreal Circus)

Ненасильственная медитативная Android-аркада и интерактивный симулятор трёх 3D-аттракционов, вдохновлённый сюрреализмом Сальвадора Дали, Рене Магритта и Маурица Эшера. В игре отсутствуют враги, таймеры выживания и экран проигрыша: каждое действие игрока рождает пентатонические звуки и визуальные эффекты.

Проект создан в Google AI Studio (`metadata.json`, `assets/.aistudio/`), подробная концепция описана в [`GDD.md`](GDD.md).

## Аттракционы и возможности

- **Ловцы Блуждающих Сфер (`Gravity Funnel`)** — управление парящей неоновой воронкой свайпами по 3D-дорожке для ловли разноцветных сфер снов.
- **Карусель Созвездий (`Celestial Carousel`)** — вращение 3D-карусели геометрических фигур и соединение звёзд одного цвета в светящиеся созвездия.
- **Эфирные Мыльные Пузыри (`Ethereal Bubbles`)** — взаимодействие с перламутровыми пузырями в искажённой гравитации (разделение свайпом, лопание касанием, слияние при столкновении).
- **Звуковой ландшафт** — синтез чистых пентатонических нот через `SurrealSoundGenerator.kt` (Android `AudioTrack` / `SoundPool`).
- **Прогрессия** — начисление очков гармонии (Harmony Points), открытие цветовых палитр и локальное сохранение сессий в базе данных Room.

## Структура репозитория

- `app/src/main/java/com/example/`:
  - `MainActivity.kt` — точка входа приложения;
  - `audio/SurrealSoundGenerator.kt` — генератор процедурного звука;
  - `data/` — слой данных Room (`dao/`, `database/`, `model/`, `repository/`);
  - `ui/screens/` — экраны `CircusDashboard.kt`, `AttractionGravityFunnel.kt`, `AttractionCelestialCarousel.kt`, `AttractionEtherealBubbles.kt`;
  - `ui/viewmodel/CircusViewModel.kt` — управление состоянием;
  - `ui/theme/` — темы и палитры Compose.
- `app/build.gradle.kts`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml` — конфигурация сборки Gradle Kotlin DSL.
- `GDD.md` — гейм-дизайнерский документ.
- `AGENTS.md` — правила разработки для ИИ-агентов.
- `metadata.json`, `assets/.aistudio/`, `.env.example` — конфигурация AI Studio и шаблон переменных окружения.

## Технологический стек и сборка

- **Платформа:** Android (`minSdk = 24`, `targetSdk = 36`, `compileSdk = 36`, `applicationId = "com.aistudio.surrealcircus.qxlmpt"`).
- **Язык и UI:** Kotlin `2.2.10`, Jetpack Compose (`composeBom = 2024.09.00`, Material 3), Navigation Compose `2.8.9`, ручная проекция 3D-точек на Compose `Canvas` без игровых движков.
- **Хранение и утилиты:** Room `2.7.0` (с KSP `2.3.5`), Kotlin Coroutines `1.10.2`, Coil Compose `2.7.0`, Retrofit / Moshi `2.12.0`.
- **Сборка и тестирование:** Gradle Kotlin DSL (AGP `9.1.1`), Roborazzi, Secrets Gradle Plugin (`GEMINI_API_KEY` загружается из секретов; образец — `.env.example`).

> **Примечание по сборке:** в репозитории отсутствует Gradle Wrapper (`gradlew`), поэтому для локальной сборки необходим установленный Gradle и Android SDK 36 (либо открытие проекта в Android Studio).

## Ветки репозитория

- `main` — основная ветка с исходным кодом Android-приложения и документацией (`GDD.md`, `AGENTS.md`).
- `codex/readme-overview-20261118` (открытый PR #2 `Add concise project README`) — содержит черновик краткого `README.md` из трёх предложений с базовым описанием игры *Surreal Circus* и ссылкой на `GDD.md`.
- `claude/agents-md` — историческая ветка (связана с уже влитым в `main` PR #1 по добавлению `AGENTS.md`).

## Лицензия

Проект распространяется под лицензией [GNU Affero General Public License v3.0](LICENSE).
