package org.apache.mybatis.enhance.sensitive.handler;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.enhance.util.TableFieldHelper;
import org.apache.mybatis.enhance.sensitive.annotation.SensitiveField;
import org.apache.mybatis.enhance.sensitive.annotation.SensitiveJSONField;
import org.apache.mybatis.enhance.sensitive.annotation.SensitiveJSONFieldKey;
import org.apache.mybatis.enhance.sensitive.annotation.SensitiveType;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * 基于实体字段注解的默认脱敏处理器。
 */
@Slf4j
public class DefaultDataMaskingHandler implements DataMaskingHandler {

    @Override
    public void doParameterMasking(Object entity) {
        mask(entity, true);
    }

    @Override
    public void doResultMasking(Object entity) {
        mask(entity, false);
    }

    private void mask(Object entity, boolean parameterPhase) {
        if (Objects.isNull(entity)) {
            return;
        }
        for (Field field : TableFieldHelper.getFields(entity.getClass())) {
            SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
            SensitiveJSONField sensitiveJsonField = field.getAnnotation(SensitiveJSONField.class);
            if (Objects.isNull(sensitiveField) && Objects.isNull(sensitiveJsonField)) {
                continue;
            }
            Object rawValue = TableFieldHelper.readValue(entity, field);
            if (Objects.isNull(rawValue)) {
                continue;
            }
            Object maskedValue = maskValue(rawValue, sensitiveField, sensitiveJsonField, parameterPhase);
            if (Objects.nonNull(maskedValue)) {
                TableFieldHelper.writeValue(entity, field, maskedValue);
            }
        }
    }

    private Object maskValue(Object rawValue, SensitiveField sensitiveField,
                             SensitiveJSONField sensitiveJsonField, boolean parameterPhase) {
        if (Objects.nonNull(sensitiveField)
                && (parameterPhase ? sensitiveField.maskingWhenSet() : sensitiveField.maskingWhenGet())) {
            return SensitiveTypeRegisty.get(sensitiveField.value()).handle(rawValue);
        }
        if (Objects.nonNull(sensitiveJsonField)
                && (parameterPhase ? sensitiveJsonField.maskingWhenSet() : sensitiveJsonField.maskingWhenGet())) {
            return processJsonField(rawValue, sensitiveJsonField);
        }
        return null;
    }

    private Object processJsonField(Object rawValue, SensitiveJSONField sensitiveJsonField) {
        try {
            Map<String, Object> values = JSONUtil.parseObj(rawValue.toString());
            for (SensitiveJSONFieldKey fieldKey : sensitiveJsonField.sensitivelist()) {
                Object oldValue = values.get(fieldKey.key());
                if (Objects.nonNull(oldValue)) {
                    SensitiveType sensitiveType = fieldKey.type();
                    values.put(fieldKey.key(), SensitiveTypeRegisty.get(sensitiveType).handle(oldValue));
                }
            }
            return JSONUtil.toJsonStr(values);
        } catch (RuntimeException exception) {
            log.warn("JSON field masking failed", exception);
            return rawValue;
        }
    }
}
