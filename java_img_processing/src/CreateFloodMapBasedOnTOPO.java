import javafx.util.Pair;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class CreateFloodMapBasedOnTOPO {

    String result_img_path;
    String nif_img_path;
    String topo_img_path;

    Dataset dataset_topo;
    Dataset dataset_nif;

    double[] topo_transformation;
    double[] nif_transformation;

    SpatialReference topo_spatial_ref_1;
    SpatialReference topo_spatial_ref_2;
    SpatialReference nif_spatial_ref_1;
    SpatialReference nif_spatial_ref_2;

    BufferedImage nif_img;
    BufferedImage result_nif_img;
    BufferedImage topo_img;


    public double[] getCoordinatesFromPixel(double x, double y, double[] transformation, SpatialReference spat_ref1, SpatialReference spat_ref2){
        /**
         * @param x,y coordinates from pixel
         * @param transformation dataset. GetGeoTransform contains xoff, a, b, yoff, d, e
         * @return a array of double, first value is longitude and second latitude
         */
        double[] results = new double[2];

        results[0] = transformation[1]*x + transformation[2]*y + transformation[0];
        results[1] = transformation[4]*x + transformation[5]*y + transformation[3];

        CoordinateTransformation t = new CoordinateTransformation(spat_ref1, spat_ref2);
        results = t.TransformPoint(results[0], results[1]);

        return results;
    }


    public double[] getPixelfromCoordinates(double logitude, double latitude, double[] transformation, SpatialReference spat1, SpatialReference spat2){
        /**
         * @return double array containing pixels were the coordinates may be found, first value is x second y
         */
        double[] result = new double[2];

        CoordinateTransformation coordTrans = new CoordinateTransformation(spat1, spat2);
        result = coordTrans.TransformPoint(logitude, latitude);

        result[0] = Math.round((result[0] - transformation[0]) / transformation[1]);
        result[1] = Math.round((result[1] - transformation[3]) / transformation[5]);

        return result;
    }

    public double calculateDistanceBetweenPoints(int x1, int y1, int x2, int y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public int getHeightFromTopo(int nif_x, int nif_y){
        /**
         * @param x,y pixels into nif image
         * @return reutrn the height value into the topi image after calculation
         *         the coordinates are obtained from the nif image, then they are applied into the topi image resulting
         *         the pixels for that coordinates - the value of that pixel is returned
         */
        double[] coordinates_from_nif = getCoordinatesFromPixel(nif_x, nif_y, this.nif_transformation, this.nif_spatial_ref_1, this.nif_spatial_ref_2);
        double[] pixels_in_topo_from_coordinates = getPixelfromCoordinates(coordinates_from_nif[0], coordinates_from_nif[1], this.topo_transformation, this.topo_spatial_ref_1, this.topo_spatial_ref_2);

        int height = new Color(this.topo_img.getRGB((int)(pixels_in_topo_from_coordinates[0]), (int)(pixels_in_topo_from_coordinates[1]))).getRed();

        return height;
    }

    public void fill(){
        int height_diff = 1;
        Stack<Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >> stack;
        /**
         * stack pair description
         * first pair pixel coordinates for each cell wich is added
         * second pair pixels of the cell that added current stack item --
         * -- second pair <null, null> if the item is added by a water-pixel
         *(use to determine the distance between 2 points)
         */


        for (int i = 0; i < this.result_nif_img.getWidth(); i++) {
            for (int j = 0; j < this.result_nif_img.getHeight(); j++) {
                if(new Color(this.result_nif_img.getRGB(i, j)).getRed() == 255){
                    int start_height = getHeightFromTopo(i, j);
//                    System.out.println(start_height);
                    stack = new Stack<>();
                    Pair<Integer, Integer> start = new Pair<>(i, j);
                    Pair<Integer, Integer> added_by = new Pair<>(null, null);
                    stack.add(new Pair<>(start, added_by));

                    while(!stack.isEmpty()){
                        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> stack_el = stack.pop();
                        Pair<Integer, Integer>xy = stack_el.getKey();
                        int x = xy.getKey();
                        int y = xy.getValue();

                        int current_color = new Color(result_nif_img.getRGB(x, y)).getRed();
                        Boolean mark = false;
                        Boolean mark_not_water = false;
                        Boolean pula = false;

                        //test if already water
                        if(current_color == 255)
                            mark = true;
                        else
                        {
                            //test if height diff is in accepted limits
                            if(new Color(result_nif_img.getRGB(x, y)).getGreen() != 255) {
                                int current_height = getHeightFromTopo(x, y);
                                Pair<Integer, Integer> added_by_pix = stack_el.getValue();
                                mark_not_water = true;
                                if (added_by_pix.getKey() == null) {
                                    if (current_height >= start_height - height_diff && current_height <= start_height + height_diff)
                                        mark = true;
                                }
                                else
                                {
                                    double distance = calculateDistanceBetweenPoints(x, y, added_by_pix.getKey(), added_by_pix.getValue());
                                    if (current_height >= start_height - (height_diff/(distance)) && current_height <= start_height + (height_diff/(distance))) {
                                        mark = true;
//                                        System.out.println("ceva");
                                    }

                                }
                            }
                        }

                        if(mark)
                        {
                            //set color of current pixel to blue
                            result_nif_img.setRGB(x, y, new Color(0, 255, 0).getRGB());
                            // add neightbours
                            if(x > 0)
                                if(mark_not_water)
                                    stack.add(new Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >(new Pair<Integer, Integer>(x-1, y), new Pair<Integer, Integer>(x, y)));
                                else
                                    stack.add(new Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >(new Pair<Integer, Integer>(x-1, y), new Pair<Integer, Integer>(null, null)));
                            if(x < result_nif_img.getWidth()-1)
                                if(mark_not_water)
                                    stack.add(new Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >(new Pair<Integer, Integer>(x+1, y), new Pair<Integer, Integer>(x, y)));
                                else
                                    stack.add(new Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >(new Pair<Integer, Integer>(x+1, y), new Pair<Integer, Integer>(null, null)));
                            if(y > 0)
                                if(mark_not_water)
                                    stack.add(new Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >(new Pair<Integer, Integer>(x, y-1), new Pair<Integer, Integer>(x, y)));
                                else
                                    stack.add(new Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >(new Pair<Integer, Integer>(x, y-1), new Pair<Integer, Integer>(null, null)));
                            if(y < result_nif_img.getHeight()-1)
                                if(mark_not_water)
                                    stack.add(new Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >(new Pair<Integer, Integer>(x, y+1), new Pair<Integer, Integer>(x, y)));
                                else
                                    stack.add(new Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >(new Pair<Integer, Integer>(x, y+1), new Pair<Integer, Integer>(null, null)));
                        }
                    }
                }
            }

        }

        String out_path_flood = "D:\\proj_licenta\\set_images_landsat\\processed_landsat";

        try {
            ImageIO.write(result_nif_img, "png", new File("D:\\proj_licenta\\set_images_landsat\\processed_landsat\\out6.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public CreateFloodMapBasedOnTOPO(String result_img_path, String nif_img_path, String topo_img_path){
        this.result_img_path = result_img_path;
        this.nif_img_path = nif_img_path;
        this.topo_img_path = topo_img_path;

        File nif_file = new File(this.nif_img_path);
        File result_nif_file = new File(this.result_img_path);
        File topo_file = new File(this.topo_img_path);

        this.nif_img = null;
        this.result_nif_img = null;
        this.topo_img = null;

        try {
            this.nif_img = ImageIO.read(nif_file);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("cannot load nif_file " + nif_img_path);
        }

        try {
            this.result_nif_img = ImageIO.read(result_nif_file);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("cannot load result_nif_file " + result_img_path);
        }

        try {
            this.topo_img = ImageIO.read(topo_file);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("cannot load topo_file " + topo_img_path);
        }



        this.dataset_nif = gdal.Open(this.nif_img_path, gdalconst.GA_ReadOnly);
        this.dataset_topo = gdal.Open(this.topo_img_path, gdalconst.GA_ReadOnly);

        this.nif_transformation = new double[2];
        this.topo_transformation = new double[2];
        this.nif_transformation = dataset_nif.GetGeoTransform();
        this.topo_transformation = dataset_topo.GetGeoTransform();

        this.nif_spatial_ref_1 = new SpatialReference();
        this.nif_spatial_ref_1.ImportFromWkt(dataset_nif.GetProjectionRef());
        this.nif_spatial_ref_2 = new SpatialReference();
        this.nif_spatial_ref_2.ImportFromEPSG(4326);

        this.topo_spatial_ref_1 = new SpatialReference();
        this.topo_spatial_ref_1.ImportFromWkt(dataset_topo.GetProjectionRef());
        this.topo_spatial_ref_2 = new SpatialReference();
        this.topo_spatial_ref_2.ImportFromEPSG(4326);

        fill();

//        System.out.println("ok");
//
//        nif_img.setRGB(50,50, new Color(0, 150, 100).getRGB());
//        Color x = new Color(nif_img.getRGB(50, 50));
//        System.out.println(x.getRed());
//        System.out.println(x.getGreen());
//        System.out.println(x.getBlue());

//
//
//        Dataset ds = gdal.Open(topo_img_path, gdalconst.GA_ReadOnly);
//        double[] transformation = ds.GetGeoTransform();
//        SpatialReference spat1 = new SpatialReference();
//        spat1.ImportFromWkt(ds.GetProjectionRef());
//
//        SpatialReference spat2 = new SpatialReference();
//        spat2.ImportFromEPSG(4326);
//
//        double[] rez1 = getCoordinatesFromPixel(0, 0, transformation, spat1, spat2);
//        double[] rez2 = getCoordinatesFromPixel(125, 130, transformation, spat1, spat2);
//
//        System.out.println("rez1");
//        System.out.println(new Double(rez1[0]).toString() + " " + new Double(rez1[1]).toString());
//        System.out.println("rez2");
//        System.out.println(new Double(rez2[0]).toString() + " " + new Double(rez2[1]).toString());
//
//        double[] rez3 = getPixelfromCoordinates(rez2[0], rez2[1], transformation, spat1, spat2);
//        System.out.println("rez3");
//        System.out.println(new Double(rez3[0]).toString() + " " + new Double(rez3[1]).toString());
    }


}
