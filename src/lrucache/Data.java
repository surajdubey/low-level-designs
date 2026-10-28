package lrucache;

public class Data {
    int value;
    long expiresAt;

    public Data(int value, long expiresAt) {
        this.value = value;
        this.expiresAt = expiresAt;
    }
}
