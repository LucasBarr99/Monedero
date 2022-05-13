package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();
  private Integer maxDepositosDiarios = 3;
  private double limiteExtraccionDiario = 1000.00;
  public Stream<Movimiento> getDepositos(){
    return this.movimientos.stream().filter(Movimiento::isDeposito);
  }

  public void depositar(double cuanto) {
    validarMontoPositivo(cuanto);
    validarDepositosDiarios();
    agregarMovimiento(LocalDate.now(),cuanto,true);
  }

  public void extraer(double cuanto) {
    validarMontoPositivo(cuanto);
    validarTopeExtraccion(cuanto);
    validarLimiteExtraccion(cuanto);
    agregarMovimiento(LocalDate.now(),cuanto,false);
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> movimiento.isExtraccion() && movimiento.esDeLaFecha(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  private void validarMontoPositivo(double monto){
    if (monto <= 0) {
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  private void validarDepositosDiarios(){
    if (getDepositos().count() >= maxDepositosDiarios) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + maxDepositosDiarios + " depositos diarios");
    }
  }

  private void validarTopeExtraccion(double monto){
    if (getSaldo() - monto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  private void validarLimiteExtraccion(double monto){
    double limiteRestante = this.limiteExtraccionDiario - getMontoExtraidoA(LocalDate.now());
    if (monto > limiteRestante) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + this.limiteExtraccionDiario
          + " diarios, límite: " + limiteRestante);
    }
  }
}
