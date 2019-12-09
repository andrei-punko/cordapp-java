
pushd build\nodes\Notary
start java -jar corda.jar
popd

pushd build\nodes\PartyA
start java -jar corda.jar
popd

pushd build\nodes\PartyB
start java -jar corda.jar
popd
