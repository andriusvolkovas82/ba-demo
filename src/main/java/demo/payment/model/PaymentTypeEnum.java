package demo.payment.model;

public enum PaymentTypeEnum {

    TYPE1(0.05, true, "https://reqres.in/api/users?delay=5"),
    TYPE2(0.1, true, "http://google.com"),
    TYPE3(0.15, false, "");

    private final double cancellationCoefficient;
    private final boolean notificationEnabled;
    private final String notificationUri;

    PaymentTypeEnum(double cancellationCoefficient, boolean notificationEnabled, String notificationUri) {
        this.cancellationCoefficient = cancellationCoefficient;
        this.notificationEnabled = notificationEnabled;
        this.notificationUri = notificationUri;
    }

    public double getCancellationCoefficient() {
        return cancellationCoefficient;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public String getNotificationUri() {
        return notificationUri;
    }
}
