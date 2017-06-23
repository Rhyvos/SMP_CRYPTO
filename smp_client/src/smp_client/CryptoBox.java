

package smp_client;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import com.sun.crypto.provider.SunJCE;

import java.util.Arrays;

public class CryptoBox
{
	SecretKey key;
	SecretKeySpec keySpec;
	
	public CryptoBox(){}
	
	public void setKey( SecretKey key )
	{
		this.key = key;
		keySpec = new SecretKeySpec( Arrays.copyOf( key.getEncoded(), 16 ), "AES" );
	}
	
	public byte[] encrypt( byte[] data )
	{
		try
		{
			Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5PADDING" );
			int ivsize = cipher.getBlockSize();
			byte[] ivbytes = new byte[ ivsize ];
		
			SecureRandom srandom = new SecureRandom();
			srandom.nextBytes( ivbytes );
		
			IvParameterSpec iv = new IvParameterSpec( ivbytes );
			//SecretKeySpec keySpec = new SecretKeySpec( key.getEncoded(), "AES" );
		
			cipher.init( Cipher.ENCRYPT_MODE, keySpec, iv );
		
			byte[] enc = cipher.doFinal( data );
			byte[] result = new byte[ ivsize + enc.length ];
			System.arraycopy( ivbytes, 0, result, 0, ivsize );
			System.arraycopy( enc, 0, result, ivsize, enc.length );
		
			return result;
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public byte[] decrypt( byte[] data )
	{
		try
		{
			Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5PADDING" );
			int ivsize = cipher.getBlockSize();
			byte[] ivbytes = new byte[ ivsize ];
			
			System.arraycopy( data, 0, ivbytes, 0, ivsize );
			
			byte[] enc = new byte[ data.length - ivsize ];
			
			System.arraycopy( data, ivsize, enc, 0, enc.length );
		
			IvParameterSpec iv = new IvParameterSpec( ivbytes );
			//SecretKeySpec keySpec = new SecretKeySpec( key.getEncoded(), "AES" );
		
			cipher.init( Cipher.DECRYPT_MODE, keySpec, iv );
		
			byte[] result = cipher.doFinal( enc );
		
			return result;
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		
		return null;
	}
}
