# Third-party model notices

## Unified violence/filth image classifier

- Model file: `app/src/main/assets/violence_filth_v1_int8.tflite`
- Model supplied as a locally trained TensorFlow Lite artifact
- Labels: `safe`, `violence_or_filth`
- Training sources recorded in the supplied model metrics:
  - `farazv2/violence-detection-violence-class`
  - `garythung/trashnet`
  - `keras/cifar10`
- Bundled model SHA-256:
  `af8b598e24f3d0538ecf83c80470c4a01bdd5082e9b9d479518586062d498050`

CuspiDroid loads this one INT8 model for all inappropriate-image decisions.
Images are processed only on the device and are not uploaded to a moderation
service. Detection is probabilistic and may produce false positives or false
negatives.
