package net.minecraft.network.encryption;

import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collection;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public interface SignatureVerifier {
   SignatureVerifier NOOP = (updatable, signatureData) -> {
      return true;
   };
   Logger LOGGER = LogUtils.getLogger();

   boolean validate(SignatureUpdatable updatable, byte[] signatureData);

   default boolean validate(byte[] signedData, byte[] signatureData) {
      return this.validate((updater) -> {
         updater.update(signedData);
      }, signatureData);
   }

   private static boolean verify(SignatureUpdatable updatable, byte[] signatureData, Signature signature) throws SignatureException {
      Objects.requireNonNull(signature);
      updatable.update(signature::update);
      return signature.verify(signatureData);
   }

   static SignatureVerifier create(PublicKey publicKey, String algorithm) {
      return (updatable, signatureData) -> {
         try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            return verify(updatable, signatureData, signature);
         } catch (Exception var5) {
            LOGGER.error("Failed to verify signature", var5);
            return false;
         }
      };
   }

   @Nullable
   static SignatureVerifier create(ServicesKeySet servicesKeySet, ServicesKeyType servicesKeyType) {
      Collection collection = servicesKeySet.keys(servicesKeyType);
      return collection.isEmpty() ? null : (updatable, signatureData) -> {
         return collection.stream().anyMatch((keyInfo) -> {
            Signature signature = keyInfo.signature();

            try {
               return verify(updatable, signatureData, signature);
            } catch (SignatureException var5) {
               LOGGER.error("Failed to verify Services signature", var5);
               return false;
            }
         });
      };
   }
}
