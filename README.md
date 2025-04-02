# scv3

compilar:
javac mySharingServer.java
javac mySharingClient.java

executar:
java mySharingServer <port>
java mySharingClient <serverAddress> <user-id> <password>



TODO

<!-- passwords hashed com salts, com cada linha tendo userId + <hash (pass + salt)> + salt
formato: <user-id>:<hash( password + salt )>:<salt>
exmeplo: <jose:efnsuwiBuk+XDCu39plD6Wn6/kUnhWQZ7/eokKUJmUQ=:xQ+KUF46offqxaQ8pIz09Q==>
 -->    DONE


sockets seguros TLS/SSL com autenticação unilateral
chaves privadas guardadas em keystores protegidas por passwords
chaves publicas auto-assinadas do servidor e dos clientes devem ser adicionadas à truststore
o servidor e cada cliente terão uma copia da truststore, contendo todos os referidos certificados



autenticidade e integridade dos ficheiros com assinaturas digitais, o ficheiro deve ser previamente assinado com a chave privada do utilizador.

as assinaturas devem ser guardadas num ficheiro adicional com extensão <signer.user-id> o qual tambem é enviado e armazenado no servidor

o download de um ou multiplos ficheiros as assinaturas tambem devem ser descarregadas e verificadas no cliente

em ambiente de desenvolvimento é aconselhado a utilização de uma keystore propria para cada utilizador designado por <keystore.user-id> guardando a chave privada e publica do utilizador



a confidencialidade dos ficheiros é assegurada atraves de criptografia hibrida. 
 - cada workspace tem uma chave criada pelo owner do workspace partilhada aos colaboradores apenas por ASSIMETRICA


 - a chave do workspace SIMETRICA é criada pelo Owner do workspace com base numa <password + salt(random)>, num esquema de Password-based Encryption <PBE>.
(sugere-se que o owner guarde a chave gerada num ficheiro de texto cifrado (com a propria chave publica) dentro do workspace).
                                        CREATE <WS> <PASSWORD>

- o método que implementa a função de adição de utilizador a um workspace deverá ser 
alterado para contemplar a criação de um ficheiro de texto com a chave do workspace, cifrá-lo com a chave pública do utilizador a adicionar e enviá-lo para o respetivo workspace. 

- Sempre que um utilizador pretender fazer um upload de ficheiros para um workspace, o servidor deverá verificar se o utilizador tem permissão de acesso. Em caso afirmativo, o servidor enviará a chave do workspace respetivo (cifrada com a chave pública do utilizador). Ao receber a chave cifrada do workspace, o utilizador terá de decifrá-la usando a sua chave privada. Com base na chave do workspace, o utilizador deve cifrar todos os ficheiros previamente e de seguida enviá-los para o workspace.

- Sempre  que  um  utilizador  pretender  fazer  um  download,  o  servidor  deverá  verificar  se  o utilizador  tem  permissão  de  acesso.  Em  caso  afirmativo,  o  servidor  enviará  os  ficheiros solicitados juntamente com a chave do workspace respetiva (cifrada com a chave pública do utilizador). Ao receber a chave cifrada do workspace, o utilizador terá de decifrá-la usando a sua  chave  privada,  e,  em  seguida,  o  utilizador  decifrará  os  ficheiros  usando  a  chave  do workspace.  
 

- Toda  criptografia  assimétrica  no  projeto  deve  usar  <RSA>  com  <chaves  de  2048  bits>.  
- A criptografia simétrica deve ser efetuada com <AES> e <chaves de 128 bits>. Para as sínteses, deve ser usado um algoritmo seguro.