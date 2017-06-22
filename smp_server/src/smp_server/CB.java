import java.io.*;

public class CB
{
	public static void main( String[] args )
	{
		DHCryptoBox aliceDH = new DHCryptoBox();
		DHCryptoBox bobDH = new DHCryptoBox();
		
		// Alicja generuje pare kluczy
		aliceDH.createKeyPair( true );
		// Alicja wysyla do Boba swoj klucz publiczny
		byte[] apk = aliceDH.getPublicKey();
		// Bob odbiera klucz publiczny Alicji i na jego podstawie generuje swoja pare kluczy
		bobDH.setAdvPublicKey( apk );
		bobDH.createKeyPair( false );
		// Bob wysyla swoj klucz publiczny
		byte[] bpk = bobDH.getPublicKey();
		// Alicja odbiera klucz publiczny Boba
		aliceDH.setAdvPublicKey( bpk );
		
		// Bob i Alicja moga wyliczyc klucze sesyjne (te same)
		// i tym samym zakonczyc protokol DH
		CryptoBox alice = new CryptoBox();
		alice.setKey( aliceDH.generateSecret() );
		
		CryptoBox bob = new CryptoBox();
		bob.setKey( bobDH.generateSecret() );
		
		// Alicja i Bob uzywaja klucza sesyjnego do szyfrowania i deszyfrowania danych
		System.out.println( new String( bob.decrypt( alice.encrypt( "Hello, world!".getBytes() ) ) ) );
		System.out.println( new String( bob.decrypt( alice.encrypt( "qwerty".getBytes() ) ) ) );
		System.out.println( new String( alice.decrypt( bob.encrypt( "some, test.".getBytes() ) ) ) );
	}
}
