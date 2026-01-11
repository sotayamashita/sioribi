# 栞日

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/sotayamashita/sioribi) [![CI](https://github.com/sotayamashita/sioribi/actions/workflows/ci.yml/badge.svg)](https://github.com/sotayamashita/sioribi/actions/workflows/ci.yml)

## 開発とテスト

- ユニットテスト: `./gradlew testDebugUnitTest`
- カバレッジレポート生成: `./gradlew testDebugUnitTest jacocoTestReport`
- カバレッジ検証: `./gradlew jacocoTestCoverageVerification`

カバレッジHTMLは `app/build/reports/jacoco/jacocoTestReport/html/index.html` に出力されます。
