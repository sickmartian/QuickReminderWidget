package com.sickmartian.quickreminderwidget;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static com.sickmartian.quickreminderwidget.CustomAlarmTimeValue.NONE_INDEX;
import static com.sickmartian.quickreminderwidget.CustomAlarmTimeValue.customValueCorrespondingNames;
import static com.sickmartian.quickreminderwidget.CustomAlarmTimeValue.customValueCorrespondingValues;

/**
 * Created by sickmartian on 8/15/16.
 */
public class CustomAlarmTimeFragment extends Fragment {
    Button customValue1Button;
    Button customValue2Button;
    Button customValue3Button;

    public static CustomAlarmTimeFragment getInstance() {
        return new CustomAlarmTimeFragment();
    }

    class ValueHolder {
        int value = QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME;
        ValueHolder(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
    }

    ValueHolder customValue1 = new ValueHolder(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
    ValueHolder customValue2 = new ValueHolder(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
    ValueHolder customValue3 = new ValueHolder(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_alarm_time_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        customValue1Button = (Button) view.findViewById(R.id.custom_value_1_button);
        customValue2Button = (Button) view.findViewById(R.id.custom_value_2_button);
        customValue3Button = (Button) view.findViewById(R.id.custom_value_3_button);

        customValue1Button.setText(R.string.custom_values_5_label);
        customValue1.setValue(QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_1);
        customValue2Button.setText(R.string.custom_values_15_label);
        customValue2.setValue(QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_2);
        customValue3Button.setText(R.string.custom_values_disabled_label);
        customValue3.setValue(QuickReminderWidgetProvider.DEFAULT_CUSTOM_TIME_3);
        showOrHideButtons();
        customValue1Button.setOnClickListener(new ValueButtonClickHandler(getActivity(),
                customValue1Button, customValue1));
        customValue2Button.setOnClickListener(new ValueButtonClickHandler(getActivity(),
                customValue2Button, customValue2));
        customValue3Button.setOnClickListener(new ValueButtonClickHandler(getActivity(),
                customValue3Button, customValue3));
    }

    private void showOrHideButtons() {
        if (customValue1.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            if (customValue2.getValue() != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
                customValue1.setValue(customValue2.getValue());
                customValue1Button.setText(customValue2Button.getText());

                customValue2.setValue(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
                customValue2Button.setText(customValueCorrespondingNames[NONE_INDEX]);
            } else if (customValue3.getValue() != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
                customValue1.setValue(customValue3.getValue());
                customValue1Button.setText(customValue3Button.getText());

                customValue3.setValue(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
                customValue3Button.setText(customValueCorrespondingNames[NONE_INDEX]);
            }
        }

        if (customValue2.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            if (customValue3.getValue() != QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
                customValue2.setValue(customValue3.getValue());
                customValue2Button.setText(customValue3Button.getText());

                customValue3.setValue(QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME);
                customValue3Button.setText(customValueCorrespondingNames[NONE_INDEX]);
            }
        }

        if (customValue2.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME &&
                customValue3.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            customValue3Button.setVisibility(View.GONE);
        } else {
            customValue3Button.setVisibility(View.VISIBLE);
        }

        if (customValue1.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME &&
                customValue2.getValue() == QuickReminderWidgetProvider.DISABLED_CUSTOM_TIME) {
            customValue2Button.setVisibility(View.GONE);
        } else {
            customValue2Button.setVisibility(View.VISIBLE);
        }
    }

    private class ValueChooserDialogHandler implements DialogInterface.OnClickListener {
        private final ValueHolder valueToChange;
        private final TextView textToChange;

        ValueChooserDialogHandler(TextView textToChange, ValueHolder valueToChange) {
            this.valueToChange = valueToChange;
            this.textToChange = textToChange;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            textToChange.setText(customValueCorrespondingNames[which]);
            valueToChange.setValue(customValueCorrespondingValues[which]);

            showOrHideButtons();
        }
    }

    private class ValueButtonClickHandler implements View.OnClickListener {
        private final ValueHolder mValueToChange;
        private final TextView mTextToChange;
        private final Context mContext;

        ValueButtonClickHandler(Context context, TextView textToChange, ValueHolder valueToChange) {
            mValueToChange = valueToChange;
            mTextToChange = textToChange;
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String[] names = new String[customValueCorrespondingNames.length];
            for (int i = 0; i < customValueCorrespondingNames.length; i++) {
                names[i] = getString(customValueCorrespondingNames[i]);
            }
            builder.setItems(names, new ValueChooserDialogHandler(mTextToChange, mValueToChange));
            builder.create().show();
        }
    }

    public static class Times {
        private int v1;
        private int v2;
        private int v3;

        Times(int v1, int v2, int v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public int getV1() {
            return v1;
        }

        public int getV2() {
            return v2;
        }

        public int getV3() {
            return v3;
        }
    }

    public Times getTimes() {
        return new Times(customValue1.getValue(), customValue2.getValue(), customValue3.getValue());
    }

}
