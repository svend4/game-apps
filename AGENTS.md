# AGENTS.md — Surreal Circus (game-apps)

## Проект

Android-игра «Сюрреалистический Цирк Снов» (Surreal Circus): три спокойных 3D-аттракциона без врагов и таймеров (Gravity Funnel, Celestial Carousel, Ethereal Bubbles). Замысел, правила и техническая часть описаны в `GDD.md`. Проект создан в Google AI Studio (`metadata.json`, `assets/.aistudio/`).

## Стек

- Kotlin, Jetpack Compose (Material 3), один модуль `app`.
- Gradle Kotlin DSL; версии библиотек в `gradle/libs.versions.toml`.
- compileSdk 36, minSdk 24, targetSdk 36; плагины KSP, Roborazzi, Secrets Gradle Plugin.
- 3D рисуется вручную: проекция точек на Compose `Canvas`, без игровых движков (`GDD.md`, раздел 5).

## Правила

- Не добавлять игровые движки и тяжёлые библиотеки без отдельного указания.
- Новые зависимости только через `gradle/libs.versions.toml`.
- Ключи не коммитить: `GEMINI_API_KEY` берётся из секретов, `.env.example` только образец.
- Не менять `metadata.json` и `assets/.aistudio/`: их использует AI Studio.

## Проверка

- В облачной среде сборка сейчас не проверяется: в репозитории нет Gradle Wrapper (`gradlew`), а для сборки нужен Android SDK 36.
- Поэтому в описании PR: что изменено, какие файлы, что проверить вручную на устройстве.

## PR

- Один PR на одну задачу. В описании: что сделано, как проверить, что осталось.
