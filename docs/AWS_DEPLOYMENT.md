# Despliegue AWS pendiente

La guia anterior dependia de una persistencia local que ya no forma parte de Pedidos360 y fue retirada para evitar ejecutar instrucciones incompatibles.

El despliegue cloud se redefinira en una etapa posterior a partir de la arquitectura Oracle validada localmente. Esta migracion no crea ni modifica EC2, API Gateway, Entra ID, IAM, redes o repositorios remotos.

Antes de redactar la nueva guia se debe decidir el servicio Oracle administrado o la estrategia de conectividad segura que reemplazara la instancia XE local. No reutilice instrucciones de despliegue anteriores.
