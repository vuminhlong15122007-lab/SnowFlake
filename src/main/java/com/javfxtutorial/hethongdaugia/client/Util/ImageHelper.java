package com.javfxtutorial.hethongdaugia.client.Util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class ImageHelper {
    // chuyển hóa từ base64 thành ảnh
    public static Image base64ToImage(String base64Data){
        if (base64Data == null || base64Data.isBlank()){
            return null;
        }
        // đoạn này AI bảo thêm vào để làm clean cho base64
        String cleanData = base64Data;
        if (base64Data.startsWith("data:")) {
            int commaIndex = base64Data.indexOf(',');
            if (commaIndex > 0) {
                cleanData = base64Data.substring(commaIndex + 1);
            }
        }
        // dù t thấy cũng không hiểu cái qq j đang diễn ra
        byte[] imageBytes = Base64.getDecoder().decode(cleanData);
        return new Image(new ByteArrayInputStream(imageBytes));
    }
    // load ảnh lên ImageView
    public static boolean loadBase64ToImageView(ImageView imageView, String base64Data) {
        if (imageView == null) {
            return false;
        }

        Image image = base64ToImage(base64Data); // chuyển hóa từ base64 thành ảnh qua method base64ToImage
        if (image != null && !image.isError()) {
            imageView.setImage(image);
            return true;
        } else {
            imageView.setImage(null);
            return false;
        }
    }
    // chuyển từ ảnh thành mã hóa base64
    public static String fileToBase64(byte[] fileContent) {
        if (fileContent == null || fileContent.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

}
