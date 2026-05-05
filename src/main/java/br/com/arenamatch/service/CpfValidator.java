package br.com.arenamatch.service;

import org.springframework.stereotype.Component;

@Component
public class CpfValidator {

    public boolean isValido(String cpf) {
        String cpfLimpo = limpar(cpf);
        if (cpfLimpo == null || cpfLimpo.length() != 11 || cpfLimpo.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito = calcularDigito(cpfLimpo, 9, 10);
        int segundoDigito = calcularDigito(cpfLimpo, 10, 11);

        return primeiroDigito == Character.getNumericValue(cpfLimpo.charAt(9))
                && segundoDigito == Character.getNumericValue(cpfLimpo.charAt(10));
    }

    private int calcularDigito(String cpf, int quantidadeDigitos, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (pesoInicial - i);
        }

        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("[^0-9]", "");
    }
}
