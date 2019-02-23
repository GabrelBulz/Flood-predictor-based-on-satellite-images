package java_classes;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Subimages_512 {

    private int nr_of_pics;

    String input_path_origin;
    String out_path_split_origin;
    String input_path_processed;
    String out_path_split_processed;




    static class Thread_subimage implements Runnable{

        String out_path;
        String img_name;
        long cont;
        int image_nr;

        public Thread_subimage(String img_name, int image_nr, String out_path){
            this.img_name = img_name;
            this.image_nr = image_nr;
            this.out_path = out_path;
            this.cont = 0;
        }

        @Override
        public void run() {
            java.io.File f = new File(this.img_name);
            BufferedImage img = null;
            try {
                img = ImageIO.read(f);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < img.getWidth()-512; i+=512) {
                for (int j = 0; j < img.getHeight()-512 ; j+=512) {
                    BufferedImage temp = img.getSubimage(i, j, 512, 512);

                    BufferedImage image_without_alpha = new BufferedImage(temp.getWidth(), temp.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

                    for (int k = 0; k < temp.getWidth(); k++) {
                        for (int l = 0; l < temp.getHeight(); l++) {
                            image_without_alpha.setRGB(k, l, temp.getRGB(l, k));
                        }

                    }

                    try {
                        ImageIO.write(image_without_alpha, "png", new File(this.out_path + "\\" + Integer.toString(this.image_nr) + "_" + Long.toString(this.cont++) + ".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }



    }

    public Subimages_512(int nr_of_pics, String input_path_origin, String out_path_split_origin, String input_path_processed, String out_path_split_processed) {
        this.nr_of_pics = nr_of_pics;
        this.input_path_origin = input_path_origin;
        this.out_path_split_origin = out_path_split_origin;
        this.input_path_processed = input_path_processed;
        this.out_path_split_processed = out_path_split_processed;


        ExecutorService excutor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < this.nr_of_pics; i++) {
            if(i == 3)
                continue;
            String image_name = this.input_path_origin + "\\" + Integer.toString(i) + "_nif.tif";
            excutor.execute(new Thread_subimage(image_name, i, this.out_path_split_origin));
        }


        for (int i = 0; i < this.nr_of_pics; i++) {
            if(i == 3)
                continue;
            String image_name_processed = this.input_path_processed + "\\" + Integer.toString(i) + ".png";
            excutor.execute(new Thread_subimage(image_name_processed, i, this.out_path_split_processed));
        }



        excutor.shutdown();
        while(!excutor.isTerminated()){

        }

        System.out.println("Finish threads");
    }

}

