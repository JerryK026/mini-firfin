CREATE INDEX idx__member__id__deleted ON member (id, deleted);
CREATE INDEX idx__member_money__id__deleted ON member_money (id, deleted);
CREATE INDEX idx__recharge_history__id__deleted ON recharge_history (id, deleted);
CREATE INDEX idx__transfer_history__id__deleted ON transfer_history (id, deleted);
CREATE INDEX idx__transfer_history__created_date_time__sender_id__id__deleted ON transfer_history (created_date_time, sender_id, id, deleted);
