package kth.ads;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONObject;

@ServerEndpoint("/adsendpoint")
public class WebSocketEndpoint {
    
    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
    private Database database;
    
    public WebSocketEndpoint(){
        database = new Database();
    }
    
    @OnMessage
    public String onMessage(String message, Session session) {
        final JSONObject data = new JSONObject(message);
        System.out.println(data);
        String type = data.getString("type");
        switch(type){
            case "login":
                String emailLogin = data.getString("email");
                String passwordLogin = data.getString("password");
                boolean login = this.database.isAccount(emailLogin, passwordLogin);
                try{
                    session.getBasicRemote().sendText("login " + login + " " + emailLogin);
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case "create":
                String emailCreate = data.getString("email");
                String passwordCreate = data.getString("password");
                boolean create = this.database.createAccount(emailCreate, passwordCreate);
                try{
                    session.getBasicRemote().sendText("create " + create);
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case "ad":
                String title = data.getString("title");
                String description = data.getString("description");
                String price = data.getString("price");
                String category = data.getString("category");
                String user = data.getString("user");
                String location = data.getString("adLocation");
                boolean ad = this.database.addAd(title, description, price, category, user, location);
                try{
                    session.getBasicRemote().sendText("ad " + ad);
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case "list":
                List <String []> ads = this.database.getAds();
                try{
                    String adsData = "";
                    for(String[] valueAd : ads){
                        adsData += "[";
                        for(String adField : valueAd){
                            adsData += "{" + adField + "}";
                        }
                        adsData += "]";
                    }
                    session.getBasicRemote().sendText("list " + adsData);
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case "get":
                try{
                    String id = data.getString("id");
                    String [] valueAd = this.database.getAd(id);
                    String adsData = "";
                    for(String adField : valueAd){
                        adsData += "{" + adField + "}";
                    }
                    session.getBasicRemote().sendText("get " + adsData);
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }
    
    @OnOpen
    public void onOpen (Session peer) {
        peers.add(peer);
    }

    @OnClose
    public void onClose (Session peer) {
        peers.remove(peer);
    }
    
}
