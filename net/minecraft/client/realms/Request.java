package net.minecraft.client.realms;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.exception.RealmsHttpException;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class Request {
   protected HttpURLConnection connection;
   private boolean connected;
   protected String url;
   private static final int READ_TIMEOUT = 60000;
   private static final int CONNECT_TIMEOUT = 5000;

   public Request(String url, int connectTimeout, int readTimeout) {
      try {
         this.url = url;
         Proxy proxy = RealmsClientConfig.getProxy();
         if (proxy != null) {
            this.connection = (HttpURLConnection)(new URL(url)).openConnection(proxy);
         } else {
            this.connection = (HttpURLConnection)(new URL(url)).openConnection();
         }

         this.connection.setConnectTimeout(connectTimeout);
         this.connection.setReadTimeout(readTimeout);
      } catch (MalformedURLException var5) {
         throw new RealmsHttpException(var5.getMessage(), var5);
      } catch (IOException var6) {
         throw new RealmsHttpException(var6.getMessage(), var6);
      }
   }

   public void cookie(String key, String value) {
      cookie(this.connection, key, value);
   }

   public static void cookie(HttpURLConnection connection, String key, String value) {
      String string3 = connection.getRequestProperty("Cookie");
      if (string3 == null) {
         connection.setRequestProperty("Cookie", key + "=" + value);
      } else {
         connection.setRequestProperty("Cookie", string3 + ";" + key + "=" + value);
      }

   }

   public Request withHeader(String name, String value) {
      this.connection.addRequestProperty(name, value);
      return this;
   }

   public int getRetryAfterHeader() {
      return getRetryAfterHeader(this.connection);
   }

   public static int getRetryAfterHeader(HttpURLConnection connection) {
      String string = connection.getHeaderField("Retry-After");

      try {
         return Integer.valueOf(string);
      } catch (Exception var3) {
         return 5;
      }
   }

   public int responseCode() {
      try {
         this.connect();
         return this.connection.getResponseCode();
      } catch (Exception var2) {
         throw new RealmsHttpException(var2.getMessage(), var2);
      }
   }

   public String text() {
      try {
         this.connect();
         String string;
         if (this.responseCode() >= 400) {
            string = this.read(this.connection.getErrorStream());
         } else {
            string = this.read(this.connection.getInputStream());
         }

         this.dispose();
         return string;
      } catch (IOException var2) {
         throw new RealmsHttpException(var2.getMessage(), var2);
      }
   }

   private String read(@Nullable InputStream in) throws IOException {
      if (in == null) {
         return "";
      } else {
         InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
         StringBuilder stringBuilder = new StringBuilder();

         for(int i = inputStreamReader.read(); i != -1; i = inputStreamReader.read()) {
            stringBuilder.append((char)i);
         }

         return stringBuilder.toString();
      }
   }

   private void dispose() {
      byte[] bs = new byte[1024];

      try {
         InputStream inputStream = this.connection.getInputStream();

         while(inputStream.read(bs) > 0) {
         }

         inputStream.close();
         return;
      } catch (Exception var9) {
         try {
            InputStream inputStream2 = this.connection.getErrorStream();
            if (inputStream2 != null) {
               while(inputStream2.read(bs) > 0) {
               }

               inputStream2.close();
               return;
            }
         } catch (IOException var8) {
            return;
         }
      } finally {
         if (this.connection != null) {
            this.connection.disconnect();
         }

      }

   }

   protected Request connect() {
      if (this.connected) {
         return this;
      } else {
         Request lv = this.doConnect();
         this.connected = true;
         return lv;
      }
   }

   protected abstract Request doConnect();

   public static Request get(String url) {
      return new Get(url, 5000, 60000);
   }

   public static Request get(String url, int connectTimeoutMillis, int readTimeoutMillis) {
      return new Get(url, connectTimeoutMillis, readTimeoutMillis);
   }

   public static Request post(String uri, String content) {
      return new Post(uri, content, 5000, 60000);
   }

   public static Request post(String uri, String content, int connectTimeoutMillis, int readTimeoutMillis) {
      return new Post(uri, content, connectTimeoutMillis, readTimeoutMillis);
   }

   public static Request delete(String url) {
      return new Delete(url, 5000, 60000);
   }

   public static Request put(String url, String content) {
      return new Put(url, content, 5000, 60000);
   }

   public static Request put(String url, String content, int connectTimeoutMillis, int readTimeoutMillis) {
      return new Put(url, content, connectTimeoutMillis, readTimeoutMillis);
   }

   public String getHeader(String header) {
      return getHeader(this.connection, header);
   }

   public static String getHeader(HttpURLConnection connection, String header) {
      try {
         return connection.getHeaderField(header);
      } catch (Exception var3) {
         return "";
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Get extends Request {
      public Get(String string, int i, int j) {
         super(string, i, j);
      }

      public Get doConnect() {
         try {
            this.connection.setDoInput(true);
            this.connection.setDoOutput(true);
            this.connection.setUseCaches(false);
            this.connection.setRequestMethod("GET");
            return this;
         } catch (Exception var2) {
            throw new RealmsHttpException(var2.getMessage(), var2);
         }
      }

      // $FF: synthetic method
      public Request doConnect() {
         return this.doConnect();
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Post extends Request {
      private final String content;

      public Post(String uri, String content, int connectTimeout, int readTimeout) {
         super(uri, connectTimeout, readTimeout);
         this.content = content;
      }

      public Post doConnect() {
         try {
            if (this.content != null) {
               this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }

            this.connection.setDoInput(true);
            this.connection.setDoOutput(true);
            this.connection.setUseCaches(false);
            this.connection.setRequestMethod("POST");
            OutputStream outputStream = this.connection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            outputStreamWriter.write(this.content);
            outputStreamWriter.close();
            outputStream.flush();
            return this;
         } catch (Exception var3) {
            throw new RealmsHttpException(var3.getMessage(), var3);
         }
      }

      // $FF: synthetic method
      public Request doConnect() {
         return this.doConnect();
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Delete extends Request {
      public Delete(String string, int i, int j) {
         super(string, i, j);
      }

      public Delete doConnect() {
         try {
            this.connection.setDoOutput(true);
            this.connection.setRequestMethod("DELETE");
            this.connection.connect();
            return this;
         } catch (Exception var2) {
            throw new RealmsHttpException(var2.getMessage(), var2);
         }
      }

      // $FF: synthetic method
      public Request doConnect() {
         return this.doConnect();
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Put extends Request {
      private final String content;

      public Put(String uri, String content, int connectTimeout, int readTimeout) {
         super(uri, connectTimeout, readTimeout);
         this.content = content;
      }

      public Put doConnect() {
         try {
            if (this.content != null) {
               this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }

            this.connection.setDoOutput(true);
            this.connection.setDoInput(true);
            this.connection.setRequestMethod("PUT");
            OutputStream outputStream = this.connection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            outputStreamWriter.write(this.content);
            outputStreamWriter.close();
            outputStream.flush();
            return this;
         } catch (Exception var3) {
            throw new RealmsHttpException(var3.getMessage(), var3);
         }
      }

      // $FF: synthetic method
      public Request doConnect() {
         return this.doConnect();
      }
   }
}
