package demo.payment.dto;

import java.util.Objects;

public class PaymentMinimalDto {

    private Long id;
    private Double cancellationFee;

    public PaymentMinimalDto() {
    }

    public PaymentMinimalDto(Long id, Double cancellationFee) {
        this.id = id;
        this.cancellationFee = cancellationFee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(Double cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMinimalDto that = (PaymentMinimalDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(cancellationFee, that.cancellationFee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cancellationFee);
    }
}
