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

        int width = Math.min(initial_img.getWidth(), diff_img.getWidth());
        int height = Math.min(initial_img.getHeight(), diff_img.getHeight());

        BufferedImage image_without_alpha = new BufferedImage(diff_img.getWidth(), diff_img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        if(initial_img != null && diff_img!= null) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    try{
                        if (new Color(diff_img.getRGB(i, j)).getGreen() == 255 && new Color(initial_img.getRGB(i, j)).getGreen() == 0) {
                            initial_img.setRGB(i, j, new Color(25, 25, 254).getRGB());
                        }
                        }catch (Exception e) {
                        System.out.println(new Integer(i).toString() + " " + new Integer(j).toString());
                        break;
                    }
//                System.out.println(new Integer(i).toString() + " " + new Integer(j).toString());
                }
            }
        }

        try {
            ImageIO.write(initial_img, "png", new File("D:\\proj_licenta\\set_images_landsat\\processed_landsat\\result.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
