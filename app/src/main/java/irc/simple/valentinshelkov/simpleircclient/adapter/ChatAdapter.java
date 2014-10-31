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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import irc.simple.valentinshelkov.simpleircclient.R;
import irc.simple.valentinshelkov.simpleircclient.data.MessageData;

public class ChatAdapter extends ArrayAdapter<MessageData> {

    public static final String AWESOME_APP = "#awesome_app ";
    public static final String HASH_TAG_REGEXP = "(#+[a-z\\d][\\w_-][^\\s\\W]*)";
    private static final Pattern HASH_TAG_PATTERN = Pattern.compile(HASH_TAG_REGEXP);
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
        if (message.getText().contains("#")) {
            parseHashTag(text);
        }
        holder.messageText.setText(text);
    }

    private void parseHashTag(Spannable text) {
        int index = 0;
        int old = 0;
        LinkedList<String> matches = new LinkedList<String>();
        Matcher m = HASH_TAG_PATTERN.matcher(text.toString());
        while (m.find()){
            matches.add(m.group());
        }
        while (index >= 0) {
            String s = matches.poll();
            if(s == null) break;
            index = text.toString().indexOf(s, old);
            if (index == -1) break;
            old = index + s.length();
            text.setSpan(new ForegroundColorSpan(Color.RED), index, index + s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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
