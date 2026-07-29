# Third-party model notices

## ScatSpotter YOLOX-nano feces detector

- Model file: `app/src/main/assets/yolox_nano_poop.tflite` (converted to
  TensorFlow Lite without changing the float32 weights)
- Upstream model: `yolox_nano_poop_cropped_only_best.onnx`
- Model source: https://github.com/Erotemic/poop_models
- Project and training-code source: https://github.com/Erotemic/shitspotter
- Training dataset: https://huggingface.co/datasets/erotemic/shitspotter
- Dataset/model attribution: Jonathan Crall, *ScatSpotter: A Dog Poop Detection Dataset*
- License stated by the upstream project for its published data and models: Creative Commons Attribution 4.0 International (CC BY 4.0)
- License text: https://creativecommons.org/licenses/by/4.0/
- Bundled model SHA-256: `ed9fad59209363ea3dc5f78e7ef73baffada5a4e512facd04a58385a008ec9f4`

The model is used only for on-device detection. Images are not uploaded to a
moderation service. Detection is probabilistic and may produce false positives
or false negatives, especially outside the outdoor dog-feces imagery represented
in the training data.
