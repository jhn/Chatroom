package mn.jhn.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPCall
{
    private final String WEATHER_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather?q=";
    private final String IP_ENDPOINT = "http://jsonip.com";
    private final String USER_AGENT = "Mozilla/5.0";

    public String makeCall(String endpoint) throws Exception
    {
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        if (con.getResponseCode() != 200)
        {
            return ">There was an error.";
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String payload;
        StringBuffer response = new StringBuffer();
        while ((payload = in.readLine()) != null)
        {
            response.append(payload);
        }
        in.close();

        return response.toString();
    }

    public String getWeatherEndpoint()
    {
        return WEATHER_ENDPOINT;
    }

    public String getIpEndpoint()
    {
        return IP_ENDPOINT;
    }

}
