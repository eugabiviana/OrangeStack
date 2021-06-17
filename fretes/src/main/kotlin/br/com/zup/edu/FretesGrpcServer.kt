package br.com.zup.edu

import com.google.rpc.Code
import com.google.protobuf.Any
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        logger.info("Calculando frete para request: $request")

        val cep = request?.cep

        if(cep == null || cep.isBlank()){
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Por favor, informe o CEP!")
                .asRuntimeException())
            return
        }


        if(!request.cep.matches("[0-9]{5}-[\\d]{3}".toRegex())){
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("CEP Inválido")
                .augmentDescription("O formato do CEP deve ser: 99999-999")
                .asRuntimeException())
            return
        }

        //SIMILAR UM VERIFICADOR DE SEGURANÇA
        if(cep.endsWith("333")){
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("Usuário não autorizado")
                .addDetails(Any.pack(
                    ErrorDetails.newBuilder()
                        .setCode(401)
                        .setMessage("Token expirado")
                        .build())
                )
                .build()

            responseObserver?.onError(
                StatusProto.toStatusRuntimeException(statusProto)
            )
        }


        val valor: Double
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.0)
            if(valor > 100.0) {
                throw IllegalStateException("Erro inesperado ao exucutar a lógica de negócio!")
            }
        } catch (e: Exception){
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e) //anexado ao Status, mas nao enviado ao Client - só tem utilidade dentro do serviço do GRPC
                .asRuntimeException())
        }


        val response = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(Random.nextDouble(from = 0.0, until = 140.0))
            .build()
        logger.info("O frete calculado para o cep ${response.cep}, é de: R$ ${response.valor}")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}