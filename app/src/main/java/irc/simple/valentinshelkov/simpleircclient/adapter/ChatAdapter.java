package irc.simple.valentinshelkov.simpleircclient.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import irc.simple.valentinshelkov.simpleircclient.R;
import irc.simple.valentinshelkov.simpleircclient.data.MessageData;

public class ChatAdapter extends ArrayAdapter<MessageData> {

    public static final String AWESOME_APP = "#awesome_app";

    public ChatAdapter(Context context, List<MessageData> data) {
        super(context, R.layout.message_text, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = newView(parent, holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        bindView(position, holder);
        return convertView;
    }

    private void bindView(int position, ViewHolder holder) {
        MessageData message = getItem(position);
        holder.messageText.setGravity(message.getTextGravity());
        holder.messageText.setTextColor(message.getTextColor());
        Spannable text = new SpannableString(message.getText());
        if (message.getText().contains(AWESOME_APP)) {
            int index = 0;
            int old = 0;
            while (index >= 0) {
                index = message.getText().indexOf(AWESOME_APP, old);
                if (index == -1) break;
                old = index + AWESOME_APP.length();
                text.setSpan(new ForegroundColorSpan(Color.RED), index, index + AWESOME_APP.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        holder.messageText.setText(text);
    }

    private View newView(ViewGroup parent, ViewHolder holder) {
        View convertView;
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_text, parent, false);
        holder.messageText = (TextView) convertView.findViewById(R.id.messageText);
        convertView.setTag(holder);
        return convertView;
    }

    private static class ViewHolder {
        TextView messageText;
    }
}
