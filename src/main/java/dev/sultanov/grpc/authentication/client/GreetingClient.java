package dev.sultanov.grpc.authentication.client;

import dev.sultanov.grpc.authentication.shared.Constants;
import dev.sultanov.grpc.authentication.shared.GreetingRequest;
import dev.sultanov.grpc.authentication.shared.GreetingResponse;
import dev.sultanov.grpc.authentication.shared.GreetingServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GreetingClient {

    public static void main(String[] args) throws UnknownHostException {
        var channel = ManagedChannelBuilder.forAddress("localhost", 8982)
                .usePlaintext()
                .build();
        var localHost = InetAddress.getLocalHost();
        String ip = localHost == null ? "" : localHost.getHostAddress();
        Map<String, Object> dataRequest = new HashMap<>();
        dataRequest.put("ip", ip);
        dataRequest.put("username", "duythai");
        dataRequest.put("password", encryptPassword("123456aA@"));
        BearerToken token = new BearerToken(getJwt(dataRequest));
        var stub = GreetingServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(token);
        GreetingRequest request = GreetingRequest.newBuilder().setName("John").build();
        GreetingResponse response = stub.greeting(request);
        System.out.println(response.getGreeting());
    }

    private static String getJwt(Map<String, Object> dataRequest) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = currentTimeMillis + 3000000;

        return Jwts.builder()
                .setClaims(dataRequest)
                .setSubject("GreetingClient")
                .signWith(SignatureAlgorithm.HS256, Constants.JWT_SIGNING_KEY)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(expirationTimeMillis))
                .compact();
    }

    private static String encryptPassword(String password) {
        try {
            // Khởi tạo đối tượng MessageDigest với thuật toán SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Mã hóa mật khẩu
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Chuyển đổi mảng byte thành chuỗi hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
