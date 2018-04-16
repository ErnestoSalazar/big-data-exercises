package nearsoft.academy.bigdata.recommendation;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {


    private BiMap<Integer, String> mapProducts = HashBiMap.create();
    private BiMap<Integer, String> mapUsers = HashBiMap.create();

    List<String> usersIdArray = new ArrayList<>();
    List<String> productsIdArray = new ArrayList<>();
    List<Double> reviewsArray = new ArrayList<>();

    private File fileInput;
    private FileWriter fileWriter;

    public MovieRecommender(String path) throws IOException {
        this.fileWriter = new FileWriter("movies.csv");
        this.readInputFile(path);
    }

    public int getTotalReviews(){
        return this.reviewsArray.size();
    }

    public int getTotalProducts(){
        return this.mapProducts.size();
    }

    public int getTotalUsers(){
        return this.mapUsers.size();
    }



    private void readInputFile(String path) throws IOException {


        String PRODUCT_ID ="product/productId: ";
        String USER_ID = "review/userId: ";
        String SCORE = "review/score: ";

        String readedLine;

        InputStream fileStream = null;
        fileStream = new FileInputStream(path);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(decoder);

        int mapUserKey = 0;
        int mapProductKey = 0;

        while((readedLine = bufferedReader.readLine()) != null){
            if(readedLine.contains(PRODUCT_ID)){
                String splitedLine = readedLine.split(PRODUCT_ID)[1];
                this.productsIdArray.add(splitedLine);
                if(!this.mapProducts.containsValue(splitedLine)){
                    this.mapProducts.put(mapProductKey, splitedLine);
                    mapProductKey++;
                }
            }else if(readedLine.contains(USER_ID)){
                String splitedLine = readedLine.split(USER_ID)[1];
                this.usersIdArray.add(splitedLine);
                if(!this.mapUsers.containsValue(splitedLine)){
                    this.mapUsers.put(mapUserKey, splitedLine);
                    mapUserKey++;
                }
            }else if(readedLine.contains(SCORE)){
                String splitedLine = readedLine.split(SCORE)[1];
                this.reviewsArray.add(Double.parseDouble(splitedLine));
            }
        }
        this.writeCSV();
        System.out.println("csv created");
        System.out.println(this.mapProducts.size());
        System.out.println(this.mapUsers.size());
        System.out.println(this.reviewsArray.size());
        this.closeCSV();
    }



    private void writeCSV(){
        try {
            String lineToAppend;
            for (int i = 0; i < this.usersIdArray.size()-1; i++) {
                Integer idUser = this.mapUsers.inverse().get(this.usersIdArray.get(i));
                Integer idProduct = this.mapProducts.inverse().get(this.productsIdArray.get(i));

                lineToAppend = idUser + "," + idProduct + "," + this.reviewsArray.get(i) + "\n";
                this.fileWriter.append(lineToAppend);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeCSV(){
        try{
            this.fileWriter.flush();
            this.fileWriter.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public List<String> getRecommendationsForUser(String userId){
        List<String> listRecommendations = new ArrayList<>();
        try {

            List<RecommendedItem> recommendations = null;

            DataModel model = new FileDataModel(new File("movies.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            recommendations = recommender.recommend(this.mapUsers.inverse().get(userId), 3);
            for (RecommendedItem recommendation : recommendations) {
                listRecommendations.add(this.mapProducts.get((int) recommendation.getItemID()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return listRecommendations;
    }


}
