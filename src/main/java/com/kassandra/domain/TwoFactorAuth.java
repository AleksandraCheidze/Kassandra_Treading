package com.kassandra.domain;

import jdk.jfr.Enabled;
import lombok.Data;

@Data
public class TwoFactorAuth {
    private boolean isEnabled = false;
    private VerificationType sendTo;
}
