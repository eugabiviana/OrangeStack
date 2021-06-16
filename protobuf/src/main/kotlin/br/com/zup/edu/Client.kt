package br.com.zup.edu

import io.grpc.ManagedChannelBuilder

fun main() {
    val channel = ManagedChannelBuilder
                    .forAddress("localhost", 50051)
                    .usePlaintext()
                    .build()


    val request = FuncionarioRequest.newBuilder()
        .setNome("Yuri Matheus")
        .setCpf("000.000.000-00")
        .setIdade(23)
        .setSalario(2000.20)
        .setAtivo(true)
        .setCargo(Cargo.DEV)
        .addEndereco(FuncionarioRequest.Endereco.newBuilder()
            .setLogradouro("Rua das Tabajaras")
            .setCep("00000-000")
            .setComplemento("Casa 20")
            .build())
        .build()

    val client = FuncionarioServiceGrpc.newBlockingStub(channel)
    val response = client.cadastrar(request)

    println(response)
    
}
