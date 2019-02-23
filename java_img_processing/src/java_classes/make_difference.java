package java_classes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class make_difference {


    String initial;
    String difference;

    public make_difference(String initial_image, String difference_image){
       this.initial = initial_image;
       this.difference = difference_image;

       this.process_difference();
    }

    public void process_difference(){
        File initial = new File(this.initial);
        File diff = new File(this.difference);
        BufferedImage initial_img = null;
        BufferedImage diff_img = null;

        // open files
        try {
            initial_img = ImageIO.read(initial);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            diff_img = ImageIO.read(diff);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(initial_img != null && diff_img != null){
            for (int i = 0; i < initial_img.getWidth(); i++) {
                for (int j = 0; j < initial_img.getHeight(); j++) {
                    if(new Color(initial_img.getRGB(i, j)).getGreen() == 255 && new Color(diff_img.getRGB(i, j)).getGreen() == 255) {
                        initial_img.setRGB(i, j, new Color(0, 0, 255).getRGB());
                        continue;
                    }
                    if(new Color(initial_img.getRGB(i, j)).getGreen() == 255 && new Color(diff_img.getRGB(i, j)).getGreen() == 0)
                    {
                        initial_img.setRGB(i, j, new Color(255, 255 , 0).getRGB());
                        continue;
                    }
                    if(new Color(initial_img.getRGB(i, j)).getGreen() == 0 && new Color(diff_img.getRGB(i, j)).getGreen() == 255)
                    {
                        initial_img.setRGB(i, j, new Color(255, 0 , 0).getRGB());
                        continue;
                    }

                }

            }
        }

        try {
            ImageIO.write(initial_img, "png", new File("D:\\proj_licenta\\set_images_landsat\\processed_landsat\\result7.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
