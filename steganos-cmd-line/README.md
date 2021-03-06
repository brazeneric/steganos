# steganos-cmd-line

## About
A command line demo of **steganos-core**

## Build
`mvn clean package`

## Usage
Try passing these arguments to `java -jar steganos-cmd-line-1.0-SNAPSHOT-jar-with-dependencies.jar`

```
# Encode/decode using the reserved-place method
reserved_place_encode tiger.png "Hello World"
reserved_place_decode tiger-encoded.png

# Encode/decode using the reserved-place method and extra conspicuousness for demo effect
reserved_place_encode tiger.png "Hello World!" 5
reserved_place_decode tiger-encoded.png 5

# Prep/encode/decode using the diff w/ prep method
diff_img_prep "tiger.png"
diff_with_prep_encode tiger-prepped.png "Hello World"
diff_decode tiger-prepped.png tiger-prepped-encoded.png

# Prep/encode/decode using the diff w/ prep method and extra conspicuousness for demo effect
diff_img_prep "tiger.png" 20
diff_with_prep_encode tiger-prepped.png "Hello World" 20
diff_decode tiger-prepped.png tiger-prepped-encoded.png

# Encode/decode using the up/down diff method
up_down_diff_encode tiger.png "Hello World"
diff_decode tiger.png tiger-encoded.png

# Encode/decode using the up/down diff method and extra conspicuousness for demo effect
up_down_diff_encode tiger.png "Hello World" 80
diff_decode tiger.png tiger-encoded.png
```