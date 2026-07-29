# Third-party model notices

## Unified inappropriate-image classifier

- Model file: `app/src/main/assets/inappropriate_image.tflite`
- Base model: `OwenElliott/image-safety-classifier-xs`
- Base-model source:
  https://huggingface.co/OwenElliott/image-safety-classifier-xs
- Architecture: SwiftFormer XS
- Base-model license: MIT
- Fecal-content adaptation data and project:
  https://github.com/Erotemic/shitspotter
- ScatSpotter dataset:
  https://huggingface.co/datasets/erotemic/shitspotter
- ScatSpotter attribution: Jonathan Crall,
  *ScatSpotter: A Dog Poop Detection Dataset*
- ScatSpotter license: Creative Commons Attribution 4.0 International
  (CC BY 4.0)
- Additional human-feces training and validation images:
  https://commons.wikimedia.org/wiki/Category:Human_feces
  (individual files remain subject to the licenses and attribution shown on
  their Wikimedia Commons file pages; the source images are not bundled)
- Mobile conversion: Google LiteRT Torch
  https://github.com/google-ai-edge/ai-edge-torch
- Bundled model SHA-256:
  `83b7056b776701c1846663cb4e4099d195f75024e670ae1fc6d8169d0f7ac583`

CuspiDroid loads this one model for all inappropriate-image decisions. Its
shared SwiftFormer backbone produces one calibrated unsafe score covering the
base model's NSFW/NSFL classes and the fecal-content adaptation. Images are
processed only on the device and are not uploaded to a moderation service.

Detection is probabilistic and may produce false positives or false negatives.
The fecal-content adaptation was evaluated with image-level train/validation
separation, including held-out human-feces images.
