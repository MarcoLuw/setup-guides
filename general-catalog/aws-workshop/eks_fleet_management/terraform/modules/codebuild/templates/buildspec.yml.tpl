version: 0.2

phases:
  pre_build:
    commands:
      - echo Build started on `date`
  build:
    commands:
      - echo Building...
  post_build:
    commands:
      - echo Build completed on `date`
