/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.art.model;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.app.job.JobScheduler;

import com.android.server.art.ArtManagerLocal;
import com.android.server.art.PriorityClass;
import com.android.server.art.ReasonMapping;
import com.android.server.pm.PackageManagerLocal;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** @hide */
@SystemApi(client = SystemApi.Client.SYSTEM_SERVER)
public class ArtFlags {
    // Common flags.

    /** Whether the operation is applied for primary dex'es. */
    public static final int FLAG_FOR_PRIMARY_DEX = 1 << 0;
    /** Whether the operation is applied for secondary dex'es. */
    public static final int FLAG_FOR_SECONDARY_DEX = 1 << 1;

    // Flags specific to `optimizePackage`.

    /** Whether to optimize dependency libraries as well. */
    public static final int FLAG_SHOULD_INCLUDE_DEPENDENCIES = 1 << 2;
    /**
     * Whether the intention is to downgrade the compiler filter. If true, the optimization will
     * be skipped if the target compiler filter is better than or equal to the compiler filter
     * of the existing optimized artifacts, or optimized artifacts do not exist.
     */
    public static final int FLAG_SHOULD_DOWNGRADE = 1 << 3;
    /**
     * Whether to force optimization. If true, the optimization will be performed regardless of
     * any existing optimized artifacts.
     */
    public static final int FLAG_FORCE = 1 << 4;
    /**
     * If set, the optimization will be performed for a single split. Otherwise, the optimization
     * will be performed for all splits. {@link OptimizeParams.Builder#setSplitName()} can be used
     * to specify the split to optimize.
     *
     * When this flag is set, {@link #FLAG_FOR_PRIMARY_DEX} must be set, and {@link
     * #FLAG_FOR_SECONDARY_DEX} and {@link #FLAG_SHOULD_INCLUDE_DEPENDENCIES} must not be set.
     */
    public static final int FLAG_FOR_SINGLE_SPLIT = 1 << 5;

    /**
     * Flags for
     * {@link ArtManagerLocal#deleteOptimizedArtifacts(PackageManagerLocal.FilteredSnapshot, String,
     * int)}.
     *
     * @hide
     */
    // clang-format off
    @IntDef(flag = true, prefix = "FLAG_", value = {
        FLAG_FOR_PRIMARY_DEX,
        FLAG_FOR_SECONDARY_DEX,
    })
    // clang-format on
    @Retention(RetentionPolicy.SOURCE)
    public @interface DeleteFlags {}

    /**
     * Default flags that are used when
     * {@link ArtManagerLocal#deleteOptimizedArtifacts(PackageManagerLocal.FilteredSnapshot,
     * String)}
     * is called.
     * Value: {@link #FLAG_FOR_PRIMARY_DEX}.
     */
    public static @DeleteFlags int defaultDeleteFlags() {
        return FLAG_FOR_PRIMARY_DEX;
    }

    /**
     * Flags for
     * {@link ArtManagerLocal#getOptimizationStatus(PackageManagerLocal.FilteredSnapshot, String,
     * int)}.
     *
     * @hide
     */
    // clang-format off
    @IntDef(flag = true, prefix = "FLAG_", value = {
        FLAG_FOR_PRIMARY_DEX,
        FLAG_FOR_SECONDARY_DEX,
    })
    // clang-format on
    @Retention(RetentionPolicy.SOURCE)
    public @interface GetStatusFlags {}

    /**
     * Default flags that are used when
     * {@link ArtManagerLocal#getOptimizationStatus(PackageManagerLocal.FilteredSnapshot, String)}
     * is called.
     * Value: {@link #FLAG_FOR_PRIMARY_DEX}.
     */
    public static @GetStatusFlags int defaultGetStatusFlags() {
        return FLAG_FOR_PRIMARY_DEX;
    }

    /**
     * Flags for {@link OptimizeParams}.
     *
     * @hide
     */
    // clang-format off
    @IntDef(flag = true, prefix = "FLAG_", value = {
        FLAG_FOR_PRIMARY_DEX,
        FLAG_FOR_SECONDARY_DEX,
        FLAG_SHOULD_INCLUDE_DEPENDENCIES,
        FLAG_SHOULD_DOWNGRADE,
        FLAG_FORCE,
        FLAG_FOR_SINGLE_SPLIT,
    })
    // clang-format on
    @Retention(RetentionPolicy.SOURCE)
    public @interface OptimizeFlags {}

    /**
     * Default flags that are used when
     * {@link OptimizeParams.Builder#Builder(String)} is called.
     *
     * @hide
     */
    public static @OptimizeFlags int defaultOptimizeFlags(@NonNull String reason) {
        switch (reason) {
            case ReasonMapping.REASON_INSTALL:
            case ReasonMapping.REASON_INSTALL_FAST:
            case ReasonMapping.REASON_INSTALL_BULK:
            case ReasonMapping.REASON_INSTALL_BULK_SECONDARY:
            case ReasonMapping.REASON_INSTALL_BULK_DOWNGRADED:
            case ReasonMapping.REASON_INSTALL_BULK_SECONDARY_DOWNGRADED:
                return FLAG_FOR_PRIMARY_DEX;
            case ReasonMapping.REASON_INACTIVE:
                return FLAG_FOR_PRIMARY_DEX | FLAG_FOR_SECONDARY_DEX | FLAG_SHOULD_DOWNGRADE;
            case ReasonMapping.REASON_FIRST_BOOT:
            case ReasonMapping.REASON_BOOT_AFTER_OTA:
                return FLAG_FOR_PRIMARY_DEX | FLAG_SHOULD_INCLUDE_DEPENDENCIES;
            case ReasonMapping.REASON_BG_DEXOPT:
            case ReasonMapping.REASON_CMDLINE:
            default:
                return FLAG_FOR_PRIMARY_DEX | FLAG_FOR_SECONDARY_DEX
                        | FLAG_SHOULD_INCLUDE_DEPENDENCIES;
        }
    }

    // Keep in sync with `PriorityClass` except for `PRIORITY_NONE`.

    /**
     * Initial value. Not expected.
     *
     * @hide
     */
    public static final int PRIORITY_NONE = -1;
    /** Indicates that the operation blocks boot. */
    public static final int PRIORITY_BOOT = PriorityClass.BOOT;
    /**
     * Indicates that a human is waiting on the result and the operation is more latency sensitive
     * than usual.
     */
    public static final int PRIORITY_INTERACTIVE_FAST = PriorityClass.INTERACTIVE_FAST;
    /** Indicates that a human is waiting on the result. */
    public static final int PRIORITY_INTERACTIVE = PriorityClass.INTERACTIVE;
    /** Indicates that the operation runs in background. */
    public static final int PRIORITY_BACKGROUND = PriorityClass.BACKGROUND;

    /**
     * Indicates the priority of an operation. The value affects the resource usage and the process
     * priority. A higher value may result in faster execution but may consume more resources and
     * compete for resources with other processes.
     *
     * @hide
     */
    // clang-format off
    @IntDef(prefix = "PRIORITY_", value = {
        PRIORITY_NONE,
        PRIORITY_BOOT,
        PRIORITY_INTERACTIVE_FAST,
        PRIORITY_INTERACTIVE,
        PRIORITY_BACKGROUND,
    })
    // clang-format on
    @Retention(RetentionPolicy.SOURCE)
    public @interface PriorityClassApi {}

    /** The job has been successfully scheduled. */
    public static final int SCHEDULE_SUCCESS = 0;

    /** @see JobScheduler#RESULT_FAILURE */
    public static final int SCHEDULE_JOB_SCHEDULER_FAILURE = 1;

    /** The job is disabled by the system property {@code pm.dexopt.disable_bg_dexopt}. */
    public static final int SCHEDULE_DISABLED_BY_SYSPROP = 2;

    /**
     * Indicates the result of scheduling a background dexopt job.
     *
     * @hide
     */
    // clang-format off
    @IntDef(prefix = "SCHEDULE_", value = {
        SCHEDULE_SUCCESS,
        SCHEDULE_JOB_SCHEDULER_FAILURE,
        SCHEDULE_DISABLED_BY_SYSPROP,
    })
    // clang-format on
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScheduleStatus {}

    private ArtFlags() {}
}
