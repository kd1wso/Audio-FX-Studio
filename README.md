# Audio FX Studio

Нативное локальное Android-приложение на Kotlin, Jetpack Compose и Media3 Transformer.

## Запуск

Откройте `AudioFxStudio` в Android Studio с JDK 17 и Android SDK 35, синхронизируйте Gradle и запустите `app`. Минимальная версия Android: API 26. Сетевые разрешения не используются.

## Возможности

SAF `ACTION_OPEN_DOCUMENT` с persistable URI permission, ExoPlayer preview через `DefaultAudioSink`, кастомные Media3 `AudioProcessor` для скорости, reverb, 8D и bass boost, Compose Material 3 dark UI, пресеты и экспорт через Transformer.

## Ограничения

Transformer использует кодеки устройства. MP3, FLAC и OGG на экспорт не гарантируются одинаково на всех моделях. WAV является наиболее предсказуемым lossless-вариантом. Текущий пример экспортирует WAV; production-версии следует добавить проверку `MediaCodecList`, выбор MIME/битрейта и отдельные SAF `CreateDocument` сценарии для форматов.

8D применяет амплитудное панорамирование и лучше воспринимается в наушниках. DSP рассчитан на PCM 16-bit interleaved; float/planar входы потребуют адаптера. Обработка полностью локальная.

